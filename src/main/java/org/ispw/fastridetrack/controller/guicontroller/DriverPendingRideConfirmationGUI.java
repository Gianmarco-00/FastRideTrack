package org.ispw.fastridetrack.controller.guicontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.ispw.fastridetrack.bean.DriverBean;
import org.ispw.fastridetrack.bean.TaxiRideConfirmationBean;
import org.ispw.fastridetrack.exception.DriverDAOException;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.model.TemporaryMemory;
import org.ispw.fastridetrack.model.enumeration.RideConfirmationStatus;
import org.ispw.fastridetrack.session.SessionManager;

import static org.ispw.fastridetrack.util.ViewPath.*;

public class DriverPendingRideConfirmationGUI {

    @FXML private TextField driverUsernameField;
    @FXML private TextField clientNameField;
    @FXML private TextField destinationField;
    @FXML private TextField estimatedFareField;
    @FXML private TextField estimatedTimeField;
    @FXML private Button destinationButton;
    @FXML private Button clientPositionButton;
    @FXML private HBox buttonBox;
    @FXML private VBox rejectBox;
    @FXML private Label confirmationLabel;

    private ApplicationFacade facade;

    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void initialize() {
        TaxiRideConfirmationBean confirmation = TemporaryMemory.getInstance().getRideConfirmation();
        if (facade == null) {
            facade = SceneNavigator.getFacade();
        }
        if(confirmation != null && confirmation.getStatus() == RideConfirmationStatus.ACCEPTED) {
            buttonBox.setVisible(false);
            confirmationLabel.setVisible(true);
        }
        displayDriverUsername();
        displayConfirmationData();
    }

    private void displayDriverUsername() {
        DriverBean driver = DriverBean.fromModel(SessionManager.getInstance().getLoggedDriver());
        if (driver != null) {
            driverUsernameField.setText(driver.getUsername());
        }
    }

    private void displayConfirmationData() {
        TaxiRideConfirmationBean confirmation = TemporaryMemory.getInstance().getRideConfirmation();
        if (confirmation != null){
            clientNameField.setText(confirmation.getClient().getName());
            destinationField.setText(confirmation.getDestination());
            estimatedTimeField.setText(confirmation.getEstimatedTime().toString());
            estimatedFareField.setText(confirmation.getEstimatedFare().toString());
        }
    }

    @FXML
    private void onAccept() {
        TaxiRideConfirmationBean confirmation = TemporaryMemory.getInstance().getRideConfirmation();
        try {
            facade.acceptRideConfirmationAndInitializeRide(confirmation);
        } catch (DriverDAOException e) {
            throw new RuntimeException(e);
        }
        //setCurrentRide();

        buttonBox.setVisible(false);           // Nasconde i bottoni
        confirmationLabel.setVisible(true);    // Mostra il messaggio
    }

    @FXML
    public void onReject() {
        TaxiRideConfirmationBean confirmation = TemporaryMemory.getInstance().getRideConfirmation();
        try {
            facade.rejectRideConfirmation(confirmation.getRideID(), confirmation.getDriver().getUserID());
        } catch (DriverDAOException e) {
            throw new RuntimeException(e);
        }

        buttonBox.setVisible(false);
        rejectBox.setVisible(true);
        destinationButton.setVisible(false);
        clientPositionButton.setVisible(false);
    }

    @FXML
    public void onRefresh() throws FXMLLoadException {
        RideConfirmationRouter rideConfirmationRouter = new RideConfirmationRouter();
        rideConfirmationRouter.routeToNextConfirmationView();
    }

    @FXML
    public void onDriverHome() throws FXMLLoadException {
        SceneNavigator.switchTo(HOMEDRIVER_FXML, "Driver Homepage");
    }
}
