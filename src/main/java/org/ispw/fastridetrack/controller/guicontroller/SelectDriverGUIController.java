package org.ispw.fastridetrack.controller.guicontroller;

import jakarta.mail.MessagingException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.event.ActionEvent;
import javafx.animation.Animation;

import javafx.util.Duration;
import org.ispw.fastridetrack.bean.*;
import org.ispw.fastridetrack.adapter.GoogleMapsAdapter;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.exception.MapServiceException;
import org.ispw.fastridetrack.exception.RideStatusUpdateException;
import org.ispw.fastridetrack.model.Map;
import org.ispw.fastridetrack.model.TaxiRideConfirmation;
import org.ispw.fastridetrack.model.TemporaryMemory;
import org.ispw.fastridetrack.model.enumeration.RideConfirmationStatus;
import org.ispw.fastridetrack.session.SessionManager;

import java.util.Optional;

import static org.ispw.fastridetrack.util.ViewPath.SELECT_TAXI_FXML;

public class SelectDriverGUIController {

    @FXML private Label driverNameLabel;
    @FXML private Label vehiclePlateLabel;
    @FXML private Label vehicleInfoLabel;
    @FXML private Label estimatedFareLabel;
    @FXML private Label estimatedTimeLabel;
    @FXML private Button confirmButton;
    @FXML public Button goBackButton;
    @FXML private WebView mapView;

    private final TemporaryMemory tempMemory;
    private TaxiRideConfirmationBean taxiRideBean;
    private Timeline pollingTimeline;
    private boolean rideStatusHandled = false;
    private static final Duration TIMEOUT_DURATION = Duration.seconds(30);
    private static final String SELECT_TAXI_TITLE = "Select Taxi";


    // Facade iniettata da SceneNavigator
    @SuppressWarnings("java:S1104") // Field injection è intenzionale per SceneNavigator
    private ApplicationFacade facade;

    // Setter usato da SceneNavigator per iniettare il facade
    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    public SelectDriverGUIController() {
        this.facade = new ApplicationFacade();
        this.tempMemory = TemporaryMemory.getInstance();
    }

    @FXML
    public void initialize() throws MapServiceException {
        this.taxiRideBean = this.tempMemory.getRideConfirmation();

        if (taxiRideBean == null || taxiRideBean.getDriver() == null) {
            showError("Missing Data", "Unable to load driver data.");
            confirmButton.setDisable(true);
            return;
        }

        // Mostra i dati iniziali
        DriverBean driver = taxiRideBean.getDriver();
        driverNameLabel.setText("Driver: " + driver.getName());
        vehicleInfoLabel.setText("Vehicle Model: " + driver.getVehicleInfo());
        vehiclePlateLabel.setText("Vehicle Plate: " + driver.getVehiclePlate());

        if (taxiRideBean.getEstimatedFare() != null) {
            estimatedFareLabel.setText(String.format("Estimated Fare: €%.2f", taxiRideBean.getEstimatedFare()));
        } else {
            estimatedFareLabel.setText("Estimated Fare: N/A");
        }

        if (taxiRideBean.getEstimatedTime() != null) {
            int totalMinutes = (int) Math.round(taxiRideBean.getEstimatedTime());
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;

            String formattedTime = (hours > 0)
                    ? String.format("Estimated Time: %dh %02dmin", hours, minutes)
                    : String.format("Estimated Time: %dmin", minutes);

            estimatedTimeLabel.setText(formattedTime);
        } else {
            estimatedTimeLabel.setText("Estimated Time: N/A");
        }

        loadMapInView();
        // REGISTRAZIONE OBSERVER per cambio stato della corsa
        tempMemory.addObserver(evt -> {
            if ("rideConfirmation".equals(evt.getPropertyName())) {
                TaxiRideConfirmationBean updatedBean = (TaxiRideConfirmationBean) evt.getNewValue();
                if (updatedBean != null) {
                    handleRideStatusUpdate(updatedBean);
                } else {
                    System.err.println("WARNING: updatedBean is null in observer!");
                }
            }
        });
        startPollingRideStatus();
    }

    private void handleRideStatusUpdate(TaxiRideConfirmationBean updatedBean) {
        if (rideStatusHandled) return; // già gestito

        rideStatusHandled = true; // segna come gestito

        Platform.runLater(() -> {
            switch (updatedBean.getStatus()) {
                case ACCEPTED -> {
                    if (pollingTimeline != null) {
                        pollingTimeline.stop();
                    }
                    showInfo("Ride Confirmed", "The driver has accepted the ride. He is on the way!");
                }
                case REJECTED -> {
                    if (pollingTimeline != null) {
                        pollingTimeline.stop();
                    }
                    showError("Ride Rejected", "The driver has rejected the ride. You can choose another one.");
                    try {
                        SceneNavigator.switchTo(SELECT_TAXI_FXML, SELECT_TAXI_TITLE);
                    } catch (FXMLLoadException e) {
                        throw new RideStatusUpdateException("Error switching screen after ride rejection.", e);
                    }
                }
                default -> {
                    // No action for PENDING
                }
            }
        });
    }

    private void startPollingRideStatus() {
        long pollingStartTime = System.currentTimeMillis();

        pollingTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            TaxiRideConfirmationBean currentBean = tempMemory.getRideConfirmation();
            if (currentBean == null) return;

            Optional<TaxiRideConfirmation> optionalUpdatedModel = SessionManager.getInstance()
                    .getTaxiRideDAO()
                    .findById(currentBean.getRideID());

            if (optionalUpdatedModel.isEmpty()) return;

            TaxiRideConfirmation updatedModel = optionalUpdatedModel.get();
            RideConfirmationStatus updatedStatus = updatedModel.getStatus();
            RideConfirmationStatus currentStatus = currentBean.getStatus();

            if (statusHasChanged(updatedStatus, currentStatus)) {
                handleStatusChange(updatedModel);
            } else if (isPendingTooLong(updatedStatus, pollingStartTime)) {
                handlePollingTimeout(updatedModel);
            }
        }));

        pollingTimeline.setCycleCount(Animation.INDEFINITE);
        pollingTimeline.play();
    }

    private boolean statusHasChanged(RideConfirmationStatus updated, RideConfirmationStatus current) {
        return updated != current;
    }

    // Timeout: ancora in stato PENDING e scaduto tempo massimo
    private boolean isPendingTooLong(RideConfirmationStatus status, long pollingStartTime) {
        long elapsed = System.currentTimeMillis() - pollingStartTime;
        return status == RideConfirmationStatus.PENDING && elapsed >= TIMEOUT_DURATION.toMillis();
    }

    private void handleStatusChange(TaxiRideConfirmation updatedModel) {
        pollingTimeline.stop(); //Fermo il polling
        TaxiRideConfirmationBean updatedBean = TaxiRideConfirmationBean.fromModel(updatedModel);
        tempMemory.setRideConfirmation(updatedBean); // notificherò l' observer
    }

    private void handlePollingTimeout(TaxiRideConfirmation updatedModel) {
        if (rideStatusHandled) return;

        rideStatusHandled = true;
        pollingTimeline.stop();

        // Forzo il ride come REJECTED e aggiorno il DB
        updatedModel.setStatus(RideConfirmationStatus.REJECTED);
        SessionManager.getInstance().getTaxiRideDAO().update(updatedModel);

        Platform.runLater(() -> {
            showError("Driver Busy", "The driver is busy with another ride. Please select another one.");
            try {
                SceneNavigator.switchTo(SELECT_TAXI_FXML, SELECT_TAXI_TITLE);
            } catch (FXMLLoadException e) {
                throw new RideStatusUpdateException("Error switching screen after timeout.", e);
            }
        });
    }

    private void loadMapInView() throws MapServiceException {
        if (taxiRideBean == null) return;

        CoordinateBean origin = taxiRideBean.getUserLocation();
        String destination = taxiRideBean.getDestination();

        GoogleMapsAdapter mapsAdapter = new GoogleMapsAdapter();

        MapRequestBean mapRequest = new MapRequestBean();
        mapRequest.setOrigin(origin);
        mapRequest.setDestination(destination);

        Map map = mapsAdapter.calculateRoute(mapRequest);

        if (map != null && map.getHtmlContent() != null) {
            mapView.getEngine().loadContent(map.getHtmlContent());
        } else {
            showError("Map Error", "Unable to load the route map.");
        }
    }

    @FXML
    private void onConfirmRide() {
        try {
            facade.confirmRideAndNotifyDriver();
            showInfo("Ride Confirmed", "The driver has been notified via email.");
            confirmButton.setDisable(true);
            goBackButton.setDisable(true);
        } catch (MessagingException | MapServiceException e) {
            showError("Error", "Error while sending email to the driver.");
        }
    }

    @FXML
    private void onGoBack(ActionEvent event) {
        try {
            SceneNavigator.switchTo(SELECT_TAXI_FXML, SELECT_TAXI_TITLE);
        } catch (FXMLLoadException e) {
            showError("Loading Error", "Error returning to taxi selection.");
        }
    }

    private void showError(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }

    private void showInfo(String title, String message) {
        showAlert(title, message, Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}








