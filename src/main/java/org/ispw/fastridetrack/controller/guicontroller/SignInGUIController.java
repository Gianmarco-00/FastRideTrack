package org.ispw.fastridetrack.controller.guicontroller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.model.enumeration.UserType;

import static org.ispw.fastridetrack.util.ViewPath.*;

public class SignInGUIController {
    @FXML
    public  Button homepageButton;
    @FXML
    public  Button nextButton;
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @SuppressWarnings("java:S1104") // Field injection is intentional for SceneNavigator
    private ApplicationFacade facade;

    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    @FXML
    public void initialize() {
        // Disabilita temporaneamente i campi di input
        usernameField.setDisable(true);
        passwordField.setDisable(true);

        Platform.runLater(() -> {
            // Metti il focus sul root (AnchorPane)
            usernameField.getScene().getRoot().requestFocus();

            // Riabilita i campi subito dopo (con leggero delay)
            Platform.runLater(() -> {
                usernameField.setDisable(false);
                passwordField.setDisable(false);
            });
        });
    }


    @FXML
    private void onNextClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            showErrorAlert("Missing Dates", "Please insert username e password.");
            return;
        }

        try {
            boolean success = facade.login(username, password);
            if (success) {
                UserType userType = facade.getLoggedUserType();
                if (userType == UserType.CLIENT) {
                    SceneNavigator.switchTo(HOMECLIENT_FXML, "Home Client");
                } else if (userType == UserType.DRIVER) {
                    SceneNavigator.switchTo(HOMEDRIVER_FXML, "Home Driver");
                }
            } else {
                showErrorAlert("Login Failed", "Credenziali errate. Riprova.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Errore di connessione", "Impossibile connettersi al database:\n" + e.getMessage());
        }
    }

    @FXML
    private void onHomepageClick() throws FXMLLoadException {
        SceneNavigator.switchTo(HOMEPAGE_FXML, "Homepage");
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}





