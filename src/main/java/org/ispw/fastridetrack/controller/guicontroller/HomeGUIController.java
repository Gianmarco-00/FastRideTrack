package org.ispw.fastridetrack.controller.guicontroller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.ispw.fastridetrack.bean.ClientBean;
import org.ispw.fastridetrack.bean.MapRequestBean;
import org.ispw.fastridetrack.bean.RideRequestBean;
import org.ispw.fastridetrack.bean.CoordinateBean;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.model.Client;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.model.TemporaryMemory;
import org.ispw.fastridetrack.session.SessionManager;
import org.ispw.fastridetrack.util.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static org.ispw.fastridetrack.util.ViewPath.*;

public class HomeGUIController implements Initializable {

    @FXML public Button checkRiderButton;
    @FXML public Button myAccountButton;
    @FXML public Button myWalletButton;
    @FXML public Button logoutButton;
    @FXML private ChoiceBox<String> rangeChoiceBox;
    @FXML private TextField destinationField;
    @FXML private Label welcomeLabel;
    @FXML private WebView mapWebView;

    private CoordinateBean currentLocation = new CoordinateBean(40.8518, 14.2681); // Default Napoli centro

    // Facade iniettata intenzionalmente da SceneNavigator
    @SuppressWarnings("java:S1104")
    private ApplicationFacade facade;

    // Setter usato da SceneNavigator per iniettare il facade
    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (facade == null) facade = SceneNavigator.getFacade();
        showGPSAlert();
        initializeChoiceBox();
        displayUserName();
        restoreTemporaryData();
        loadCurrentLocationMap();
    }

    private void displayUserName() {
        Client client = SessionManager.getInstance().getLoggedClient();
        if (client != null) {
            welcomeLabel.setText("Benvenuto, " + client.getName());
        }
    }

    private void showGPSAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("GPS");
        alert.setHeaderText("Attiva il GPS");
        alert.setContentText("Per continuare, attiva il GPS del dispositivo.");
        alert.showAndWait();
    }

    private void initializeChoiceBox() {
        List<Integer> ranges = List.of(1, 2, 3, 5);
        rangeChoiceBox.getItems().addAll(ranges.stream().map(km -> km + " km").toList());
        rangeChoiceBox.setValue("1 km");
    }

    private void restoreTemporaryData() {
        MapRequestBean bean = TemporaryMemory.getInstance().getMapRequestBean();
        if (bean != null) {
            if (bean.getDestination() != null && !bean.getDestination().isBlank()) {
                destinationField.setText(bean.getDestination());
            }
            String radiusStr = bean.getRadiusKm() + " km";
            if (rangeChoiceBox.getItems().contains(radiusStr)) {
                rangeChoiceBox.setValue(radiusStr);
            }
            if (bean.getOrigin() != null) {
                currentLocation = bean.getOrigin();
            }
        }
    }

    private void loadCurrentLocationMap() {
        new Thread(() -> {
            try {
                String ip = IPFetcher.getPublicIP();
                var coordModel = IPLocationService.getCoordinateFromIP(ip);
                currentLocation = new CoordinateBean(coordModel.getLatitude(), coordModel.getLongitude());

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
    private void onCheckRider() throws FXMLLoadException {
        String destination = destinationField.getText();

        if (destination == null || destination.trim().isEmpty()) {
            destinationField.setStyle("-fx-border-color: red;");
            showAlert("Inserisci una destinazione valida.");
            return;
        } else {
            destinationField.setStyle(""); // Rimuovo il bordo rosso se corretto
        }

        int radiusKm = convertRangeToInt(rangeChoiceBox.getValue());
        if (radiusKm == -1) {
            showAlert("Raggio selezionato non valido.");
            return;
        }

        Client loggedClient = SessionManager.getInstance().getLoggedClient();
        if (loggedClient == null) {
            showAlert("Sessione utente non valida. Effettua nuovamente il login.");
            SceneNavigator.switchTo(HOMEPAGE_FXML, "Homepage");
            return;
        }

        String pickupLocationStr = currentLocation != null
                ? currentLocation.getLatitude() + "," + currentLocation.getLongitude()
                : "";

        // Creo i Bean
        RideRequestBean rideRequestBean = new RideRequestBean(currentLocation, destination.trim(), radiusKm, PaymentMethod.CASH);
        rideRequestBean.setClient(ClientBean.fromModel(loggedClient));
        rideRequestBean.setPickupLocation(pickupLocationStr);
        rideRequestBean.setDestination(destination.trim());

        MapRequestBean mapRequestBean = new MapRequestBean(currentLocation, destination.trim(), radiusKm);

        try {
            // Uso il Facade per salvare la richiesta e ottenere la mappa
            String html = facade.processRideRequestAndReturnMapHtml(rideRequestBean, mapRequestBean);

            if (html == null || html.isBlank()) {
                showAlert("Errore nel calcolo o visualizzazione del percorso.");
                return;
            }

            mapWebView.getEngine().loadContent(html);

            // Salvo i dati temporanei e navigo alla schermata successiva
            TemporaryMemory.getInstance().setMapRequestBean(mapRequestBean);
            SceneNavigator.switchTo(SELECT_TAXI_FXML, "Seleziona Taxi");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore durante l'elaborazione della richiesta.");
        }
    }


    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attenzione");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private int convertRangeToInt(String range) {
        try {
            return Integer.parseInt(range.split(" ")[0]);
        } catch (NumberFormatException | NullPointerException e) {
            return -1;
        }
    }

    @FXML
    private void onMyWallet() throws FXMLLoadException {
        SceneNavigator.switchTo(WALLET_FXML, "Wallet");
    }

    @FXML
    private void onMyAccount() throws FXMLLoadException {
        SceneNavigator.switchTo(ACCOUNT_FXML, "Account");
    }

    @FXML
    private void onLogout() throws FXMLLoadException {
        SessionManager.getInstance().clearSession();
        TemporaryMemory.getInstance().clear();
        SceneNavigator.switchTo(HOMEPAGE_FXML, "Homepage");
    }
}




