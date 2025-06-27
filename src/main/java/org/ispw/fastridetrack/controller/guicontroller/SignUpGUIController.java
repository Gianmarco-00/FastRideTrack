package org.ispw.fastridetrack.controller.guicontroller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import org.ispw.fastridetrack.exception.FXMLLoadException;

import java.net.URL;
import java.util.ResourceBundle;

import static org.ispw.fastridetrack.util.ViewPath.HOMEPAGE_FXML;

public class SignUpGUIController implements Initializable {

    @FXML public TextField firstNameField;
    @FXML public TextField lastNameField;
    @FXML public TextField usernameField;
    @FXML public TextField passwordField;
    @FXML private TextField phoneNumberField;
    @FXML public TextField emailField;
    @FXML private ChoiceBox<String> userTypeChoiceBox;

    // Facade iniettata da SceneNavigator
    @SuppressWarnings("java:S1104") // Field injection is intentional for SceneNavigator
    private ApplicationFacade facade;

    // Setter usato da SceneNavigator per iniettare il facade
    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inizializzo solo con "Client"
        userTypeChoiceBox.getItems().add("Client");
        userTypeChoiceBox.setValue("Client");

        // Restringo phoneNumberField ai numeri
        phoneNumberField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) {
                phoneNumberField.setText(newText.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    private void onHomepage() throws FXMLLoadException {
        SceneNavigator.switchTo(HOMEPAGE_FXML, "Homepage");
    }

    @FXML
    private void onSignUp() throws FXMLLoadException {
        // Qui posso anche validare i campi e poi ritornare alla schermata di homepage per effettuare il sign in
        SceneNavigator.switchTo(HOMEPAGE_FXML, "Homepage");
    }
}


