package org.ispw.fastridetrack.controller.guicontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.ispw.fastridetrack.bean.RideBean;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.session.SessionManager;

import static org.ispw.fastridetrack.util.ViewPath.*;

public class DriverCurrentRideGUIController {

    @FXML private Label destinationLabel;
    @FXML private Label clientNameLabel;

    @FXML private VBox rideInitiatedBox;
    @FXML private VBox clientLocatedBox;

    private ApplicationFacade facade;

    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void initialize() {
        RideBean rideBean = RideBean.fromModel(SessionManager.getInstance().getDriverSessionContext().getCurrentRide());
        if (facade == null) {
            facade = SceneNavigator.getFacade();
        }
        showRideInfo(rideBean.getDestination(),rideBean.getClient().getName());
        switch (rideBean.getStatus()) {
            case INITIATED -> {
                rideInitiatedBox.setVisible(true);
                clientLocatedBox.setVisible(false);
            }
            case CLIENT_LOCATED -> {
                rideInitiatedBox.setVisible(false);
                clientLocatedBox.setVisible(true);
            }
            default -> {
                rideInitiatedBox.setVisible(false);
                clientLocatedBox.setVisible(false);
            }
        }
    }

    @FXML
    private void showRideInfo(String destination, String clientName) {
        destinationLabel.setText(destination);
        clientNameLabel.setText(clientName);
    }

    @FXML
    private void onHomeDriver() throws FXMLLoadException {
        SceneNavigator.switchTo(HOMEDRIVER_FXML,"Homepage Driver");
    }

    @FXML
    private void onSetupClientLocationOnMap() throws FXMLLoadException {
        SceneNavigator.switchTo(HOMEDRIVER_FXML, "Homepage Driver");
    }
}
