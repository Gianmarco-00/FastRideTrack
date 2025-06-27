package org.ispw.fastridetrack.controller.guicontroller;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.ispw.fastridetrack.exception.FXMLLoadException;


import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.ispw.fastridetrack.util.ViewPath.SIGNIN_FXML;
import static org.ispw.fastridetrack.util.ViewPath.SIGNUPAS_FXML;

public class HomepageGUIController implements Initializable {

    @FXML public Button signInButton;
    @FXML public Button signUpButton;
    @FXML private ImageView imageView;

    // Facade iniettata intenzionalmente da SceneNavigator
    @SuppressWarnings("java:S1104")
    private ApplicationFacade facade;

    // Setter usato da SceneNavigator per iniettare il facade
    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/ispw/fastridetrack/images/Homepage.jpg")));
        imageView.setImage(image);
    }

    @FXML
    public void onSignInClick() throws FXMLLoadException {
        SceneNavigator.switchTo(SIGNIN_FXML,"Sign in");
    }

    @FXML
    public void onSignUpClick() throws FXMLLoadException {
        SceneNavigator.switchTo(SIGNUPAS_FXML,"Sign up as...");
    }
}

