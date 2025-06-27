package org.ispw.fastridetrack.controller.guicontroller;

import jakarta.mail.MessagingException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.event.ActionEvent;


import org.ispw.fastridetrack.bean.*;
import org.ispw.fastridetrack.adapter.GoogleMapsAdapter;
import org.ispw.fastridetrack.exception.DatabaseConnectionException;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.exception.MapServiceException;
import org.ispw.fastridetrack.model.Map;
import org.ispw.fastridetrack.model.TemporaryMemory;

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

    // Facade iniettata da SceneNavigator
    @SuppressWarnings("java:S1104") // Field injection è intenzionale per SceneNavigator
    private ApplicationFacade facade;

    // Setter usato da SceneNavigator per iniettare il facade
    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    public SelectDriverGUIController() throws DatabaseConnectionException {
        this.facade = new ApplicationFacade();
        this.tempMemory = TemporaryMemory.getInstance();
    }

    @FXML
    public void initialize() throws MapServiceException {
        this.taxiRideBean = tempMemory.getRideConfirmation();

        if (taxiRideBean == null || taxiRideBean.getDriver() == null) {
            showError("Dati mancanti", "Impossibile caricare i dati del driver.");
            confirmButton.setDisable(true);
            return;
        }

        // **Qui cambia: driver è DriverBean, non Model**
        DriverBean driver = taxiRideBean.getDriver();
        driverNameLabel.setText("Driver: " + driver.getName());
        vehicleInfoLabel.setText("Vehicle Model: " + driver.getVehicleInfo());
        vehiclePlateLabel.setText("Vehicle Plate: " + driver.getVehiclePlate());
        // Mostro il prezzo stimato con due decimali
        if (taxiRideBean.getEstimatedFare() != null) {
            estimatedFareLabel.setText(String.format("Estimated Fare: €%.2f", taxiRideBean.getEstimatedFare()));
        } else {
            estimatedFareLabel.setText("Estimated Fare: N/D");
        }
        // Mostro il tempo stimato in ore e minuti
        if (taxiRideBean.getEstimatedTime() != null) {
            int totalMinutes = (int) Math.round(taxiRideBean.getEstimatedTime());
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;

            String formattedTime;
            if (hours > 0) {
                formattedTime = String.format("Estimated Time: %dh %02dmin", hours, minutes);
            } else {
                formattedTime = String.format("Estimated Time: %dmin", minutes);
            }

            estimatedTimeLabel.setText(formattedTime);
        } else {
            estimatedTimeLabel.setText("Estimated Time: N/D");
        }

        loadMapInView();
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
            showError("Errore mappa", "Impossibile caricare la mappa del percorso.");
        }
    }

    @FXML
    private void onConfirmRide() {
        try {
            facade.confirmRideAndNotifyDriver();
            showInfo("Corsa confermata", "Il driver è stato notificato via email.");
            confirmButton.setDisable(true);
            goBackButton.setDisable(true);
        } catch (MessagingException | MapServiceException e) {
            showError("Errore", "Errore durante l'invio dell'email al driver.");
        }
    }

    @FXML
    private void onGoBack(ActionEvent event) {
        try {
            SceneNavigator.switchTo(SELECT_TAXI_FXML, "Seleziona Taxi");
        } catch (FXMLLoadException e) {
            e.printStackTrace();
            showError("Errore caricamento", "Errore nel tornare alla selezione del taxi.");
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








