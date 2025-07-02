package org.ispw.fastridetrack.controller.guicontroller;

import javafx.fxml.FXML;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
import org.ispw.fastridetrack.exception.FXMLLoadException;

public class DriverSignUpGUIController {
    @SuppressWarnings("java:S1104") // Field injection è intenzionale per SceneNavigator
    private ApplicationFacade facade;

    // Setter usato da SceneNavigator per iniettare il facade
    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void onSubmit() throws FXMLLoadException {
        SceneNavigator.switchTo("/org/ispw/fastridetrack/views/Homepage.fxml", "Homepage");
    }

    @FXML
    private void onSignUp() throws FXMLLoadException {
        SceneNavigator.switchTo("/org/ispw/fastridetrack/views/Homepage.fxml", "Homepage");
    }
}
