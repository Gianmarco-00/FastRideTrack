package org.ispw.fastridetrack.controller.guicontroller;

import javafx.scene.control.Alert;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.session.SessionManager;

import static org.ispw.fastridetrack.util.ViewPath.*;

public class CurrentRideRouter {

    private final ApplicationFacade facade;

    public CurrentRideRouter() {
        this.facade = SceneNavigator.getFacade();
    }

    public void manageCurrentRideView() throws FXMLLoadException {
        boolean isAcativeRide = SessionManager.getInstance().getDriverSessionContext().hasActiveRide();

        if(!isAcativeRide){
            showErrorAlert("No active ride found", "No current active ride has been found!");
        }else{
            SceneNavigator.switchTo(DRIVERCURRENTRIDE_FXML, "Corsa corrente");
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
