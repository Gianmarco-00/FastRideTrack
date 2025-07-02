package org.ispw.fastridetrack.controller.guicontroller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
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

    @FXML private AnchorPane rootPane;

    // Facade iniettata da SceneNavigator
    @SuppressWarnings("java:S1104") // Field injection is intentional for SceneNavigator
    private ApplicationFacade facade;

    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userTypeChoiceBox.getItems().add("Client");
        userTypeChoiceBox.setValue("Client");

        phoneNumberField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) {
                phoneNumberField.setText(newText.replaceAll("[^\\d]", ""));
            }
        });

        // Disabilito temporaneamente i campi per evitare focus automatico
        firstNameField.setDisable(true);
        lastNameField.setDisable(true);
        usernameField.setDisable(true);
        passwordField.setDisable(true);
        phoneNumberField.setDisable(true);
        emailField.setDisable(true);
        userTypeChoiceBox.setDisable(true);

        Platform.runLater(() -> {
            // Togli il focus da ogni campo mettendolo sul rootPane
            rootPane.requestFocus();

            // Riabilita i campi subito dopo
            Platform.runLater(() -> {
                firstNameField.setDisable(false);
                lastNameField.setDisable(false);
                usernameField.setDisable(false);
                passwordField.setDisable(false);
                phoneNumberField.setDisable(false);
                emailField.setDisable(false);
                userTypeChoiceBox.setDisable(false);
            });
        });

    }

    @FXML
    private void onHomepage() throws FXMLLoadException {
        SceneNavigator.switchTo(HOMEPAGE_FXML, "Homepage");
    }

    @FXML
    private void onSignUp() throws FXMLLoadException {
        SceneNavigator.switchTo(HOMEPAGE_FXML, "Homepage");
    }
}


