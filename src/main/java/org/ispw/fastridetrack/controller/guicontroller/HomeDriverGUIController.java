package org.ispw.fastridetrack.controller.guicontroller;

import jakarta.mail.MessagingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.ispw.fastridetrack.bean.DriverBean;
import org.ispw.fastridetrack.bean.RideBean;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
import org.ispw.fastridetrack.exception.ClientNotFetchedException;
import org.ispw.fastridetrack.exception.DriverDAOException;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.exception.MapServiceException;
import org.ispw.fastridetrack.model.Map;
import org.ispw.fastridetrack.util.DriverSessionContext;
import org.ispw.fastridetrack.session.SessionManager;
import org.ispw.fastridetrack.util.IPFetcher;
import org.ispw.fastridetrack.util.IPLocationService;
import org.ispw.fastridetrack.util.MapHTMLGenerator;

import static org.ispw.fastridetrack.util.ViewPath.*;

public class HomeDriverGUIController {
    @FXML private Label driverUsername;
    @FXML private WebView mapWebView;
    @FXML private TextField startpointField;
    @FXML private TextField destinationField;
    @FXML private Button routeSetupButton;
    @FXML private HBox rideActiveBox;
    @FXML private Rectangle activeRideBlock;
    @FXML private Label rideStatusLabel;
    @FXML private Label noConfirmationLabel;
    @FXML private VBox noConfirmationBox;
    @FXML private VBox finderBox;


    private ApplicationFacade facade;

    public void setFacade(ApplicationFacade facade) { this.facade = facade; }

    public void initialize() throws MapServiceException {
        if (facade == null) {
            facade = SceneNavigator.getFacade();
        }
        showUsername();

        if (DriverSessionContext.getInstance().hasActiveRide()) {
            setupActiveRideUI();

            RideBean rideBean = DriverSessionContext.getInstance().getCurrentRide();
            loadMapFromActiveRide(rideBean);

        } else if (DriverSessionContext.getInstance().getStartPoint() != null &&
                DriverSessionContext.getInstance().getEndPoint() != null) {
            setupFinderUI();

            loadMapFromStartEnd();

        } else {
            loadCurrentLocationMap();
        }

    }

    private void showUsername(){
        DriverBean driver = DriverBean.fromModel(SessionManager.getInstance().getLoggedDriver());
        if (driver != null) {
            driverUsername.setText(driver.getName());
        }
    }

    private void loadMapFromActiveRide(RideBean rideBean) throws MapServiceException {
        noConfirmationLabel.setVisible(false);
        Map map = facade.loadDriverRouteBasedOnRideStatus();
        if (map == null || map.getHtmlContent() == null) {
            showError("Impossibile caricare la mappa del percorso.");
            return;
        }

        mapWebView.getEngine().loadContent(map.getHtmlContent());

        String startpoint = facade.getAddressFromCoordinatesString(map.getOrigin());
        String destination = facade.getAddressFromCoordinatesString(map.getDestination());

        double time = map.getEstimatedTimeMinutes();

        switch (rideBean.getRideStatus()) {
            case INITIATED -> manageInitiateState(startpoint, destination, time);
            case CLIENT_LOCATED -> manageClientLocatedState(startpoint, destination);
            case ONGOING -> manageOngoingState(startpoint, destination);
            case FINISHED -> loadCurrentLocationMap();
        }
    }

    private void manageInitiateState(String startPoint, String endPoint, double estimatedTimeMinutes) {
        startpointField.setText(startPoint);
        destinationField.setText(endPoint);
        rideStatusLabel.setText("locating client");
        routeSetupButton.setText("Confirm route to client");
        routeSetupButton.setDisable(false);

        routeSetupButton.setOnAction(e -> {
            routeSetupButton.setDisable(true);  // evita doppio click
            try {
                facade.markClientLocated(estimatedTimeMinutes);

                // Ricarica lo stato aggiornato e aggiorna tutta la UI
                RideBean updatedRide = DriverSessionContext.getInstance().getCurrentRide();
                loadMapFromActiveRide(updatedRide);

                showInfo("Client location confirmed", "A confirmation email has been sent to the Client");
            } catch (MessagingException | MapServiceException ex) {
                showAlert("Errore", "Impossibile inviare email di conferma", Alert.AlertType.ERROR);
            } catch (IllegalStateException ex) {
                showAlert("Attenzione", "Client già localizzato", Alert.AlertType.WARNING);
            } finally {
                routeSetupButton.setDisable(false);
            }
        });
    }

    public void showInfo(String title, String text){
        showAlert(title,text, Alert.AlertType.INFORMATION);
    }

    private void manageClientLocatedState(String startPoint, String endPoint) {
        startpointField.setText(startPoint);
        destinationField.setText(endPoint);
        rideStatusLabel.setText("client located");
        routeSetupButton.setText("Click to start the ride");
        routeSetupButton.setDisable(false);

        routeSetupButton.setOnAction(e -> {
            routeSetupButton.setDisable(true);
            try {
                facade.startRide();

                RideBean updatedRide = DriverSessionContext.getInstance().getCurrentRide();
                loadMapFromActiveRide(updatedRide);

            } catch (ClientNotFetchedException | MapServiceException ex) {
                showAlert("Ride start failed","The client has not yet been fetched", Alert.AlertType.ERROR);
            } finally {
                routeSetupButton.setDisable(false);
            }
        });
    }


    private void manageOngoingState(String startPoint, String endPoint) {
        startpointField.setText(startPoint);
        destinationField.setText(endPoint);
        rideStatusLabel.setText("ride in progress");
        routeSetupButton.setText("Finish the ride");
        routeSetupButton.setDisable(false);

        routeSetupButton.setOnAction(e ->
            routeSetupButton.setDisable(true)
        );
    }


    private void loadMapFromStartEnd() throws MapServiceException {
        Map map = facade.loadDriverRoute();
        if (map != null && map.getHtmlContent() != null) {
            mapWebView.getEngine().loadContent(map.getHtmlContent());
        } else {
            showError("Impossibile caricare la mappa del percorso.");
        }

        String startPoint = facade.getAddressFromCoordinatesString(map.getOrigin());
        String endPoint = facade.getAddressFromCoordinatesString(map.getDestination());


        startpointField.setText(startPoint);
        destinationField.setText(endPoint);
        routeSetupButton.setOnAction(e -> {
            try {
                SceneNavigator.switchTo(DRIVERPENDINGREQUEST_FXML,"Pending Confirmation");
            } catch (FXMLLoadException ex) {
                showAlert("Error loading scene: " , ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void setupActiveRideUI() {
        noConfirmationBox.setVisible(false);
        rideActiveBox.setVisible(true);
        noConfirmationBox.setVisible(false);
        finderBox.setVisible(true);
    }

    private void setupFinderUI() {
        finderBox.setVisible(true);
        noConfirmationBox.setVisible(false);
    }

    private void loadCurrentLocationMap() {
        new Thread(() -> {
            try {
                String ip = IPFetcher.getPublicIP();
                System.out.println("IP pubblico: " + ip);

                var coordModel = IPLocationService.getCoordinateFromIP(ip);

                System.out.println("Coordinate ottenute: " + coordModel.getLatitude() + ", " + coordModel.getLongitude());

                Platform.runLater(() -> {
                    try {
                        String html = MapHTMLGenerator.generateMapHtmlString(coordModel);
                        WebEngine engine = mapWebView.getEngine();
                        engine.setJavaScriptEnabled(true);
                        engine.loadContent(html);
                    } catch (Exception e) {
                        showError("Errore nella generazione della mappa dinamica.");
                    }
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // re-imposto il flag
                Platform.runLater(() ->
                    showError("Operazione interrotta. Verrà caricata la mappa di default.")
                );
            } catch (Exception e) {
                Platform.runLater(() ->
                    showError("Impossibile recuperare la posizione. Verrà caricata la mappa di default.")
                );
            }
        }).start();
    }

    @FXML
    private void onCurrentRide() throws FXMLLoadException {
        CurrentRideRouter rideRouter = new CurrentRideRouter();
        rideRouter.manageCurrentRideView();
    }

    private void routeToNextConfirmation() throws FXMLLoadException, DriverDAOException {
        RideConfirmationRouter confirmationRouter = new RideConfirmationRouter();
        confirmationRouter.routeToNextConfirmationView();
    }

    @FXML
    private void onPendingRequests() throws FXMLLoadException, DriverDAOException {
        routeToNextConfirmation();
    }

    @FXML
    private void onViewConfirmations() throws FXMLLoadException, DriverDAOException {
        routeToNextConfirmation();
    }

    @FXML
    private void onLogout() throws FXMLLoadException {
        SessionManager.getInstance().clearSession();
        DriverSessionContext.getInstance().clear();
        SceneNavigator.switchTo(HOMEPAGE_FXML, "Homepage");
    }

    private void showError(String msg){
        showAlert("Attention",msg, Alert.AlertType.ERROR);

    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
