package org.ispw.fastridetrack.controller.guicontroller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.exception.SceneSwitchException;

import static org.ispw.fastridetrack.util.ViewPath.HOMECLIENT_FXML;

public class MyWalletGUIController {

    // Facade iniettata intenzionalmente da SceneNavigator
    @SuppressWarnings("java:S1104")
    private ApplicationFacade facade;

    // Setter usato da SceneNavigator per iniettare il facade
    public void setFacade(ApplicationFacade facade) {
        this.facade = facade;
    }

    @FXML
    public Button btnBack;

    @FXML
    public Button btnConfirm;

    @FXML
    private CheckBox checkBoxCash;

    @FXML
    private CheckBox checkBoxCard;

    @FXML
    public void initialize() {
        // Rende le checkbox mutuamente esclusive
        checkBoxCash.setOnAction(e -> {
            if (checkBoxCash.isSelected()) {
                checkBoxCard.setSelected(false);
            }
        });

        checkBoxCard.setOnAction(e -> {
            if (checkBoxCard.isSelected()) {
                checkBoxCash.setSelected(false);
            }
        });
    }

    @FXML
    void onBackPressed(ActionEvent event) throws FXMLLoadException {
        SceneNavigator.switchTo(HOMECLIENT_FXML, "Home");
    }

    @FXML
    void onConfirmPressed(ActionEvent event) throws FXMLLoadException {
        if (checkBoxCard.isSelected()) {
            showAddCardDialog();
        } else if (checkBoxCash.isSelected()) {
            System.out.println("Cash payment selected.");
            SceneNavigator.switchTo(HOMECLIENT_FXML, "Home");
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Payment method not selected");
            alert.setContentText("Please select a payment method.");
            alert.showAndWait();
        }
    }

    private void showAddCardDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Card");
        dialog.setHeaderText("Enter your card details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField cardNumber = new TextField();
        cardNumber.setPromptText("Card Number");

        TextField cardHolder = new TextField();
        cardHolder.setPromptText("Cardholder");

        TextField expiryDate = new TextField();
        expiryDate.setPromptText("MM/YY");

        PasswordField cvv = new PasswordField();
        cvv.setPromptText("CVV");

        grid.add(new Label("Card Number:"), 0, 0);
        grid.add(cardNumber, 1, 0);
        grid.add(new Label("Cardholder:"), 0, 1);
        grid.add(cardHolder, 1, 1);
        grid.add(new Label("Expiry Date:"), 0, 2);
        grid.add(expiryDate, 1, 2);
        grid.add(new Label("CVV:"), 0, 3);
        grid.add(cvv, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);

        Runnable validateInputs = () -> {
            boolean valid = isCardNumberValid(cardNumber.getText())
                    && !cardHolder.getText().trim().isEmpty()
                    && isExpiryDateValid(expiryDate.getText())
                    && isCvvValid(cvv.getText());
            addButton.setDisable(!valid);
        };

        cardNumber.textProperty().addListener((obs, oldV, newV) -> validateInputs.run());
        cardHolder.textProperty().addListener((obs, oldV, newV) -> validateInputs.run());
        expiryDate.textProperty().addListener((obs, oldV, newV) -> validateInputs.run());
        cvv.textProperty().addListener((obs, oldV, newV) -> validateInputs.run());

        addButton.setDisable(true);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String errorMessage = validateCardInputs(
                        cardNumber.getText(),
                        cardHolder.getText(),
                        expiryDate.getText(),
                        cvv.getText()
                );
                if (errorMessage != null) {
                    showError(errorMessage);
                    return null;
                }

                System.out.println("Card added:");
                System.out.println("Number: " + cardNumber.getText());
                System.out.println("Holder: " + cardHolder.getText());
                System.out.println("Expiry: " + expiryDate.getText());
                System.out.println("CVV: " + cvv.getText());

                try {
                    SceneNavigator.switchTo(HOMECLIENT_FXML, "Home");
                } catch (FXMLLoadException e) {
                    throw new SceneSwitchException("Error switching to Home scene", e);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private String validateCardInputs(String number, String holder, String expiry, String cvv) {
        if (!isCardNumberValid(number)) {
            return "Invalid card number. It must contain only digits (13-19)!";
        }
        if (holder == null || holder.trim().isEmpty()) {
            return "Cardholder cannot be empty!";
        }
        if (!isExpiryDateValid(expiry)) {
            return "Invalid or expired date! Use MM/YY.";
        }
        if (!isCvvValid(cvv)) {
            return "Invalid CVV! It must be 3 or 4 digits.";
        }
        return null;
    }

    // Funzione per validare numero carta (solo cifre, lunghezza 13-19)
    private boolean isCardNumberValid(String number) {
        return number != null && number.matches("\\d{13,19}");
    }

    // Funzione per validare formato e validità della scadenza MM/YY
    private boolean isExpiryDateValid(String expiry) {
        if (expiry == null) return false;
        if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) return false;

        // Controllo data non scaduta
        String[] parts = expiry.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]) + 2000; // es. "24" -> 2024

        java.time.YearMonth now = java.time.YearMonth.now();
        java.time.YearMonth cardDate = java.time.YearMonth.of(year, month);

        return !cardDate.isBefore(now);
    }

    // Funzione per validare CVV (3 o 4 cifre)
    private boolean isCvvValid(String cvv) {
        return cvv != null && cvv.matches("\\d{3,4}");
    }

    // Metodo per mostrare alert di errore
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}



