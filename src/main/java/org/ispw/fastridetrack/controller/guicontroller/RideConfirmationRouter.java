package org.ispw.fastridetrack.controller.guicontroller;

import javafx.scene.control.Alert;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.model.TemporaryMemory;
import org.ispw.fastridetrack.session.SessionManager;
import org.ispw.fastridetrack.bean.DriverBean;
import org.ispw.fastridetrack.bean.TaxiRideConfirmationBean;

import java.util.Optional;

import static org.ispw.fastridetrack.util.ViewPath.*;

public class RideConfirmationRouter {

    private final ApplicationFacade facade;

    public RideConfirmationRouter() {
        this.facade = SceneNavigator.getFacade();
    }

    public void routeToNextConfirmationView() throws FXMLLoadException {
        SessionManager session = SessionManager.getInstance();
        DriverBean driver = DriverBean.fromModel(session.getLoggedDriver());
        boolean isAcceptedConfirmation = session.getDriverSessionContext().hasPendingConfirmation();
        boolean isActiveRide = session.getDriverSessionContext().hasActiveRide();
        TaxiRideConfirmationBean acceptedConfirmation;

        // 1. Verifica se il driver è disponibile
        if (!driver.isAvailable()) {
            if (isActiveRide && isAcceptedConfirmation) {
                acceptedConfirmation = TaxiRideConfirmationBean.fromModel(session.getDriverSessionContext().getCurrentConfirmation());
                TemporaryMemory.getInstance().setRideConfirmation(acceptedConfirmation);
                SceneNavigator.switchTo(DRIVERPENDINGREQUEST_FXML, "Corsa accettata");
                return;
            }
            else if (isActiveRide) {
                showErrorAlert("Ride already active", "Driver has already an active ride!");
                return;
            }
            // opzionale: schermata che mostra "non disponibile"
            showErrorAlert("Driver unavailable", "Driver is currently set as unavailable.");
            return;
        }

        // 2. Verifica se c'è una ride confirmation già accettata
        if (isAcceptedConfirmation) {
            acceptedConfirmation = TaxiRideConfirmationBean.fromModel(session.getDriverSessionContext().getCurrentConfirmation());
            TemporaryMemory.getInstance().setRideConfirmation(acceptedConfirmation);
            SceneNavigator.switchTo(DRIVERPENDINGREQUEST_FXML, "Corsa accettata");
            return;
        }

        // 3. Cerca nuova conferma pending (FIFO)
        Optional<TaxiRideConfirmationBean> confirmation = facade.getNextRideConfirmation(driver.getUserID());

        if (confirmation.isPresent()) {
            TemporaryMemory.getInstance().setRideConfirmation(confirmation.get());
            SceneNavigator.switchTo(DRIVERPENDINGREQUEST_FXML, "Conferma in attesa");
        } else {
            SceneNavigator.switchTo(DRIVERNOREQUEST_FXML, "Nessuna richiesta disponibile");
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
