package org.ispw.fastridetrack.controller.guicontroller;

import javafx.fxml.FXML;
import org.ispw.fastridetrack.exception.FXMLLoadException;

public class DriverSignUpGUIController {

    @FXML
    private void onSubmit() throws FXMLLoadException {
        SceneNavigator.switchTo("/org/ispw/fastridetrack/views/Homepage.fxml", "Homepage");
    }

    @FXML
    private void onSignUp() throws FXMLLoadException {
        SceneNavigator.switchTo("/org/ispw/fastridetrack/views/Homepage.fxml", "Homepage");
    }
}
