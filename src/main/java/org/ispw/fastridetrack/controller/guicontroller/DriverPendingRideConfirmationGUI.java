package org.ispw.fastridetrack.controller.guicontroller;

import jakarta.mail.MessagingException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.ispw.fastridetrack.bean.DriverBean;
import org.ispw.fastridetrack.bean.LocationBean;
import org.ispw.fastridetrack.bean.TaxiRideConfirmationBean;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
import org.ispw.fastridetrack.exception.*;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.model.enumeration.RideConfirmationStatus;
import org.ispw.fastridetrack.util.DriverSessionContext;
import org.ispw.fastridetrack.session.SessionManager;

import static org.ispw.fastridetrack.util.ViewPath.*;

public class DriverPendingRideConfirmationGUI {

    @FXML private Label driverUsernameField;
    @FXML private TextField clientNameField;
    @FXML private TextField destinationField;
    @FXML private TextField estimatedFareField;
    @FXML private TextField estimatedTimeField;
    @FXML private TextField paymentInfoField;
    @FXML private Button destinationButton;
    @FXML private Button clientPositionButton;
    @FXML private Button acceptButton;
    @FXML private Label rideRejectedLabel;
    @FXML private HBox buttonBox;
    @FXML private VBox rejectBox;
    @FXML private VBox acceptBox;
    @FXML public Label confirmationLabel;
    @FXML private TextField reasonTextField;
    @FXML private Button refreshButton;

    private ApplicationFacade facade;
    private static final String ERROR_CONFIRM = "Error accepting confirmation";

    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void initialize() {
        TaxiRideConfirmationBean confirmation = DriverSessionContext.getInstance().getCurrentConfirmation();
        if (facade == null) {
            facade = SceneNavigator.getFacade();
        }
        if(confirmation != null && confirmation.getStatus() == RideConfirmationStatus.ACCEPTED) {
            buttonBox.setVisible(false);
            destinationButton.setVisible(false);
            clientPositionButton.setVisible(false);
            acceptBox.setVisible(true);
        }
        displayDriverUsername();
        displayConfirmationData();
    }

    private void displayDriverUsername() {
        DriverBean driver = DriverBean.fromModel(SessionManager.getInstance().getLoggedDriver());
        if (driver != null) {
            driverUsernameField.setText(driver.getName());
        }
    }

    private void displayConfirmationData() {
        TaxiRideConfirmationBean confirmation = DriverSessionContext.getInstance().getCurrentConfirmation();
        if (confirmation != null) {
            clientNameField.setText(confirmation.getClient().getName());
            destinationField.setText(confirmation.getDestination());
            estimatedTimeField.setText(confirmation.getEstimatedTime().toString());
            estimatedFareField.setText(confirmation.getEstimatedFare().toString());

            if (confirmation.getPaymentMethod() == PaymentMethod.CASH) {
                paymentInfoField.setText("Cash");
            } else if (confirmation.getPaymentMethod() == PaymentMethod.CARD) {
                paymentInfoField.setText("Card");
            }
        }
    }

    @FXML
    private void onAccept() throws DriverDAOException, MapServiceException, MessagingException {
        TaxiRideConfirmationBean confirmation = DriverSessionContext.getInstance().getCurrentConfirmation();
        try{
            facade.acceptRideConfirmationAndInitializeRide(confirmation);

            buttonBox.setVisible(false);
            acceptBox.setVisible(true);
            destinationButton.setVisible(false);
            clientPositionButton.setVisible(false);
        } catch (DriverUnavailableException | RideAlreadyActiveException |
                 RideConfirmationNotFoundException | RideConfirmationNotPendingException e) {
            showAlert(ERROR_CONFIRM, e.getMessage(), Alert.AlertType.ERROR);
            manageRideConfirmationNotAvailable();
        }
    }

    public void manageRideConfirmationNotAvailable() {
        buttonBox.setVisible(false);
        rideRejectedLabel.setVisible(true);
        acceptBox.setVisible(false);
        refreshButton.setVisible(true);
    }

    @FXML
    public void onRideDetails() throws FXMLLoadException {
        newCurrentRideRouter();
    }

    @FXML
    public void onReject() {
        buttonBox.setVisible(false);
        rideRejectedLabel.setVisible(true);
        rejectBox.setVisible(true);
        destinationButton.setVisible(false);
        clientPositionButton.setVisible(false);
    }

    @FXML
    public void onSaveText(){
        TaxiRideConfirmationBean confirmation = DriverSessionContext.getInstance().getCurrentConfirmation();
        String userInput = reasonTextField.getText();
        if(userInput.isEmpty()){
            reasonTextField.setPromptText("Please enter a reason");
            return;
        }
        try {
            facade.rejectRideConfirmation(confirmation, userInput);
        } catch (MessagingException e) {
            showAlert("Error rejecting confirmation", e.getMessage(), Alert.AlertType.WARNING);
        }
        rejectBox.setVisible(false);
        refreshButton.setVisible(true);
        DriverSessionContext.getInstance().setCurrentConfirmation(null);
    }

    @FXML
    private void onViewClientPosition(){
        LocationBean start = new LocationBean(DriverSessionContext.getInstance().getCurrentConfirmation().getDriver().getCoordinate());
        LocationBean end = new LocationBean(DriverSessionContext.getInstance().getCurrentConfirmation().getClient().getCoordinate());

        DriverSessionContext.getInstance().setStartPoint(start);
        DriverSessionContext.getInstance().setEndPoint(end);
        try{
            SceneNavigator.switchTo(HOMEDRIVER_FXML,"Driver homepage");
        }catch(Exception e){
            showAlert("Error uploading map","Can't process the map towards the client's position", Alert.AlertType.ERROR);
            acceptButton.setDisable(true);
        }
    }

    @FXML
    private void onViewDestination(){
        LocationBean start = new LocationBean(DriverSessionContext.getInstance().getCurrentConfirmation().getDriver().getCoordinate());
        LocationBean end = new LocationBean(DriverSessionContext.getInstance().getCurrentConfirmation().getDestination());

        DriverSessionContext.getInstance().setStartPoint(start);
        DriverSessionContext.getInstance().setEndPoint(end);
        try{
            SceneNavigator.switchTo(HOMEDRIVER_FXML,"Driver homepage");
        }catch(Exception e){
            showAlert("Error uploading map","Can't process the map towards the destination", Alert.AlertType.ERROR);
            acceptButton.setDisable(true);
        }
    }

    private void newCurrentRideRouter() throws FXMLLoadException {
        CurrentRideRouter currentRideRouter = new CurrentRideRouter();
        currentRideRouter.manageCurrentRideView();
    }

    @FXML
    public void onRefresh() throws FXMLLoadException, DriverDAOException {
        RideConfirmationRouter rideConfirmationRouter = new RideConfirmationRouter();
        rideConfirmationRouter.routeToNextConfirmationView();
    }

    @FXML
    public void onDriverHome() throws FXMLLoadException {
        SceneNavigator.switchTo(HOMEDRIVER_FXML, "Driver Homepage");
    }

    @FXML
    public void onCurrentRide() throws FXMLLoadException {
        newCurrentRideRouter();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
