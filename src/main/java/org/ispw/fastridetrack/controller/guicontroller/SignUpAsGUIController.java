package org.ispw.fastridetrack.controller.guicontroller;

import javafx.fxml.FXML;
import org.ispw.fastridetrack.exception.FXMLLoadException;

import static org.ispw.fastridetrack.util.ViewPath.HOMEPAGE_FXML;
import static org.ispw.fastridetrack.util.ViewPath.SIGNUPCLIENT_FXML;

public class SignUpAsGUIController {

    // Facade iniettata da SceneNavigator
    @SuppressWarnings("java:S1104") // Field injection is intentional for SceneNavigator
    private ApplicationFacade facade;

    // Setter usato da SceneNavigator per iniettare il facade
    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    @FXML
    public void onClientSignUp() throws FXMLLoadException {
        // Cambio scena a SignUp.fxml per il client
        SceneNavigator.switchTo(SIGNUPCLIENT_FXML, "Sign Up Client");
    }

    @FXML
    public void onDriverSignUp() throws FXMLLoadException {
        // Cambio scena a ?????.fxml per il driver (quella di Alex)
        SceneNavigator.switchTo("/org/ispw/fastridetrack/views/Sign Up.fxml", "Sign Up Driver");
    }

    @FXML
    public void onBackToHomepage() throws FXMLLoadException {
        // Torno alla schermata Homepage
        SceneNavigator.switchTo(HOMEPAGE_FXML, "Homepage");
    }
}


