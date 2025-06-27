package org.ispw.fastridetrack.controller.guicontroller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import org.ispw.fastridetrack.bean.*;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.model.Map;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.session.SessionManager;
import org.ispw.fastridetrack.model.TemporaryMemory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static org.ispw.fastridetrack.util.ViewPath.HOMECLIENT_FXML;
import static org.ispw.fastridetrack.util.ViewPath.SELECT_DRIVER_FXML;

public class SelectTaxiGUIController implements Initializable {

    @FXML public Button confirmRideButton;
    @FXML public Button goBackButton;
    @FXML private WebView mapView;
    @FXML private TableView<AvailableDriverBean> taxiTable;
    @FXML private TableColumn<AvailableDriverBean, String> driverNameColumn;
    @FXML private TableColumn<AvailableDriverBean, String> carModelColumn;
    @FXML private TableColumn<AvailableDriverBean, String> plateColumn;
    @FXML private TableColumn<AvailableDriverBean, String> etaColumn;
    @FXML private TableColumn<AvailableDriverBean, String> priceColumn;
    @FXML private ChoiceBox<String> paymentChoiceBox;
    @FXML private TextField destinationField;


    private final TemporaryMemory tempMemory = TemporaryMemory.getInstance();
    private final ObservableList<AvailableDriverBean> driversFX = FXCollections.observableArrayList();

    @SuppressWarnings("java:S1104")
    private ApplicationFacade facade;

    public void initialize(URL url, ResourceBundle rb) {
        taxiTable.setItems(driversFX);
        initializeTable();
        initializePaymentChoices();
        destinationField.setEditable(false);

        tempMemory.addObserver(evt -> {
            if ("availableDrivers".equals(evt.getPropertyName())) {
                @SuppressWarnings("unchecked")
                List<AvailableDriverBean> newDrivers = (List<AvailableDriverBean>) evt.getNewValue();
                Platform.runLater(() -> driversFX.setAll(newDrivers != null ? newDrivers : List.of()));
            }
        });
    }

    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
        try {
            Map map = facade.loadMapAndAvailableDriversForClient();
            MapRequestBean mapRequestBean = tempMemory.getMapRequestBean();
            destinationField.setText(mapRequestBean.getDestination());
            mapView.getEngine().loadContent(map.getHtmlContent());
            List<AvailableDriverBean> drivers = tempMemory.getAvailableDrivers();
            driversFX.setAll(drivers != null ? drivers : List.of());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore nel caricamento della mappa o dei driver.");
        }
    }


    private void initializeTable() {
        driverNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getName()));
        carModelColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getVehicleInfo()));
        plateColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getVehiclePlate()));
        // ETA come stringa formattata (es. "1h 05min" o "45min")
        etaColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getEstimatedTimeFormatted()));
        // Prezzo formattato (es. "€12.30")
        priceColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getEstimatedPriceFormatted()));
    }


    private void initializePaymentChoices() {
        paymentChoiceBox.getItems().clear();

        // Popolo con versioni leggibili dell'enum PaymentMethod (es. "Cash", "Card")
        for (PaymentMethod pm : PaymentMethod.values()) {
            String displayName = pm.name().charAt(0) + pm.name().substring(1).toLowerCase();
            paymentChoiceBox.getItems().add(displayName);
        }

        // Se c'è un metodo pagamento selezionato in memoria, lo seleziono
        PaymentMethod selected = tempMemory.getSelectedPaymentMethod();
        if (selected != null) {
            String displayName = selected.name().charAt(0) + selected.name().substring(1).toLowerCase();
            paymentChoiceBox.getSelectionModel().select(displayName);
        } else {
            paymentChoiceBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onConfirmRide() {
        AvailableDriverBean selectedDriver = taxiTable.getSelectionModel().getSelectedItem();
        if (selectedDriver == null) {
            showAlert("Seleziona un driver dalla tabella.");
            return;
        }

        String paymentMethodStr = paymentChoiceBox.getSelectionModel().getSelectedItem();
        if (paymentMethodStr == null || paymentMethodStr.isEmpty()) {
            showAlert("Seleziona un metodo di pagamento.");
            return;
        }

        PaymentMethod paymentMethodEnum;
        try {
            paymentMethodEnum = PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            showAlert("Metodo di pagamento non valido.");
            return;
        }

        ClientBean currentClient = ClientBean.fromModel(SessionManager.getInstance().getLoggedClient());
        if (currentClient == null) {
            showAlert("Utente non loggato o client non trovato.");
            return;
        }
        try {
            facade.confirmRideRequest(selectedDriver, paymentMethodEnum);
            SceneNavigator.switchTo(SELECT_DRIVER_FXML, "Conferma Driver");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore nella conferma della corsa.");
        }

    }

    @FXML
    private void onGoBackHome() {
        try {
            SceneNavigator.switchTo(HOMECLIENT_FXML, "Home");
        } catch (FXMLLoadException e) {
            e.printStackTrace();
            showAlert("Errore nel ritorno alla schermata Home.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attenzione");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}








