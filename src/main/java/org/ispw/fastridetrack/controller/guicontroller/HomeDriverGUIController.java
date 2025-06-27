package org.ispw.fastridetrack.controller.guicontroller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.ispw.fastridetrack.bean.CoordinateBean;
import org.ispw.fastridetrack.bean.DriverBean;
import org.ispw.fastridetrack.bean.RideBean;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.model.TemporaryMemory;
import org.ispw.fastridetrack.session.SessionManager;
import org.ispw.fastridetrack.util.IPFetcher;
import org.ispw.fastridetrack.util.IPLocationService;
import org.ispw.fastridetrack.util.MapHTMLGenerator;

import java.net.URL;

import static org.ispw.fastridetrack.util.ViewPath.*;

public class HomeDriverGUIController {
    @FXML private TextField driverUsername;
    @FXML private WebView mapWebView;
    @FXML private TextField startpointField;
    @FXML private TextField destinationField;
    @FXML private Button routeSetupButton;

    private CoordinateBean currentLocation = new CoordinateBean(40.8518, 14.2681);

    private ApplicationFacade facade;

    public void setFacade(ApplicationFacade facade) { this.facade = facade; }

    public void initialize(){
        boolean isActiveRide = SessionManager.getInstance().getDriverSessionContext().hasActiveRide();
        if (facade == null) {
            facade = SceneNavigator.getFacade();
        }

        loadCurrentLocationMap();
        
        if(isActiveRide) {
            RideBean rideBean = RideBean.fromModel(SessionManager.getInstance().getDriverSessionContext().getCurrentRide());
            switch (rideBean.getStatus()){
                case INITIATED -> {
                    manageInitiateState();
                }
            }
        }
        showUsername();
    }

    private void showUsername(){
        DriverBean driver = DriverBean.fromModel(SessionManager.getInstance().getLoggedDriver());
        if (driver != null) {
            driverUsername.setText(driver.getUsername());
        }
    }

    private void manageInitiateState(){
        destinationField.setEditable(false);
        routeSetupButton.setText("Confirm route to client");
    }

    private void loadCurrentLocationMap() {
        new Thread(() -> {
            try {
                String ip = IPFetcher.getPublicIP();
                System.out.println("IP pubblico: " + ip);

                var coordModel = IPLocationService.getCoordinateFromIP(ip);
                currentLocation = new CoordinateBean(coordModel.getLatitude(), coordModel.getLongitude());

                System.out.println("Coordinate ottenute: " + coordModel.getLatitude() + ", " + coordModel.getLongitude());

                Platform.runLater(() -> {
                    try {
                        String html = MapHTMLGenerator.generateMapHtmlString(coordModel);
                        WebEngine engine = mapWebView.getEngine();
                        engine.setJavaScriptEnabled(true);
                        engine.loadContent(html);
                    } catch (Exception e) {
                        showAlert("Errore nella generazione della mappa dinamica.");
                    }
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // re-imposto il flag
                Platform.runLater(() -> {
                    showAlert("Operazione interrotta. Verrà caricata la mappa di default.");
                    loadMapWithDefaultLocation();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Impossibile recuperare la posizione. Verrà caricata la mappa di default.");
                    loadMapWithDefaultLocation();
                });
            }

        }).start();
    }

    private void loadMapWithDefaultLocation() {
        Platform.runLater(() -> {
            WebEngine engine = mapWebView.getEngine();
            engine.setJavaScriptEnabled(true);
            URL url = getClass().getResource("/org/ispw/fastridetrack/html/map.html");
            if (url != null) {
                engine.load(url.toExternalForm());
            } else {
                showAlert("File map.html non trovato nelle risorse.");
            }
        });
    }

    @FXML
    private void onCurrentRide() throws FXMLLoadException {
        CurrentRideRouter rideRouter = new CurrentRideRouter();
        rideRouter.manageCurrentRideView();
    }

    @FXML
    private void onPendingRequests() throws FXMLLoadException {
        RideConfirmationRouter confirmationRouter = new RideConfirmationRouter();
        confirmationRouter.routeToNextConfirmationView();
    }

    @FXML
    private void onLogout() throws FXMLLoadException {
        SessionManager.getInstance().clearSession();
        TemporaryMemory.getInstance().clear();
        SceneNavigator.switchTo(HOMEPAGE_FXML, "Homepage");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attenzione");
        alert.setContentText(msg);
        alert.showAndWait();
    }

}
