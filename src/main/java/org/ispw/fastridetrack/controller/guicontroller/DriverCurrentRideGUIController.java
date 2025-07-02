package org.ispw.fastridetrack.controller.guicontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.ispw.fastridetrack.bean.RideBean;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
import org.ispw.fastridetrack.exception.DriverDAOException;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.util.DriverSessionContext;

import static org.ispw.fastridetrack.util.ViewPath.*;

public class DriverCurrentRideGUIController {

    @FXML private Label driverNameLabel;
    @FXML private Label destinationLabel;
    @FXML private Label clientNameLabel;
    @FXML private Label phoneNumberLabel;
    @FXML private Label emailLabel;
    @FXML private Button viewLocationButton;
    @FXML private VBox rideInitiatedBox;
    @FXML private VBox clientLocatedBox;
    @FXML private VBox rideStartedBox;
    @FXML private VBox rideFinishedBox;

    private ApplicationFacade facade;

    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void initialize() {
        RideBean rideBean = DriverSessionContext.getInstance().getCurrentRide();
        if (facade == null) {
            facade = SceneNavigator.getFacade();
        }
        showDriverName(rideBean.getDriver().getName());
        showRideInfo(rideBean.getDestination(),rideBean.getClient().getName(),rideBean.getClient().getPhoneNumber(), rideBean.getClient().getEmail());
        switch (rideBean.getRideStatus()) {
            case INITIATED -> {
                rideInitiatedBox.setVisible(true);
                clientLocatedBox.setVisible(false);
            }
            case CLIENT_LOCATED -> {
                rideInitiatedBox.setVisible(false);
                clientLocatedBox.setVisible(true);
            }
            case ONGOING -> {
                rideInitiatedBox.setVisible(false);
                rideStartedBox.setVisible(true);
                viewLocationButton.setText("view destination route on map");
            }
            case  FINISHED -> {
                rideInitiatedBox.setVisible(false);
                rideFinishedBox.setVisible(true);
                viewLocationButton.setText("go to homepage");
            }
            default -> {
                rideInitiatedBox.setVisible(false);
                clientLocatedBox.setVisible(false);
            }
        }
    }

    @FXML
    private void showDriverName(String driverName) {
        driverNameLabel.setText(driverName);
    }

    @FXML
    private void showRideInfo(String destination, String clientName,  String phoneNumber, String email) {
        destinationLabel.setText(destination);
        clientNameLabel.setText(clientName);
        phoneNumberLabel.setText(phoneNumber);
        emailLabel.setText(email);
    }

    @FXML
    private void onPendingConfirmation() throws DriverDAOException, FXMLLoadException {
        new RideConfirmationRouter().routeToNextConfirmationView();
    }

    @FXML
    private void onHomeDriver() throws FXMLLoadException {
        SceneNavigator.switchTo(HOMEDRIVER_FXML,"Homepage Driver");
    }

    @FXML
    private void onSetupLocationOnMap() throws FXMLLoadException {
        try{
            SceneNavigator.switchTo(HOMEDRIVER_FXML,"Driver homepage");
        }catch(Exception e){
            showAlert("Error uploading map","Cannot process viable route", Alert.AlertType.ERROR);
        }
    }
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
