package org.ispw.fastridetrack.controller.guicontroller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.ispw.fastridetrack.bean.DriverBean;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.model.Client;
import org.ispw.fastridetrack.session.SessionManager;

import static org.ispw.fastridetrack.util.ViewPath.*;

public class DriverNoPendingRideConfirmationGUI {

    @FXML
    TextField driverUsernameField;

    private ApplicationFacade facade;

    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void initialize() {
        if (facade == null) {
            facade = SceneNavigator.getFacade();
        }
        displayDriverUsername();
    }

    private void displayDriverUsername() {
        DriverBean driver = DriverBean.fromModel(SessionManager.getInstance().getLoggedDriver());
        if (driver != null) {
            driverUsernameField.setText(driver.getUsername());
        }
    }

    @FXML
    public void onDriverHome() throws FXMLLoadException {
        SceneNavigator.switchTo(HOMEDRIVER_FXML, "DriverHome");
    }

    @FXML
    public void onCurrentRide()throws FXMLLoadException{

    }

    public void tryNewConfirmation() throws FXMLLoadException{
        RideConfirmationRouter rideConfirmationRouter = new RideConfirmationRouter();
        rideConfirmationRouter.routeToNextConfirmationView();
    }

    @FXML
    public void onPendingRequests() throws FXMLLoadException {
       tryNewConfirmation();
    }

    @FXML
    public void onRefresh() throws FXMLLoadException {
        tryNewConfirmation();
    }
}

