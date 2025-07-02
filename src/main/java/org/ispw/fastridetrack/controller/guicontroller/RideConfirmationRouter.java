package org.ispw.fastridetrack.controller.guicontroller;

import javafx.scene.control.Alert;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
import org.ispw.fastridetrack.exception.DriverDAOException;
import org.ispw.fastridetrack.exception.FXMLLoadException;
import org.ispw.fastridetrack.util.DriverSessionContext;
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

    public void routeToNextConfirmationView() throws FXMLLoadException, DriverDAOException {
        SessionManager session = SessionManager.getInstance();
        DriverSessionContext driverContext = DriverSessionContext.getInstance();
        DriverBean driver = DriverBean.fromModel(session.getLoggedDriver());
        boolean isAcceptedConfirmation = facade.isConfirmationAccepted();
        boolean isActiveRide = facade.isActiveRide();

        if (!driver.isAvailable()) {
            if (isActiveRide && isAcceptedConfirmation) {
                driverContext.getCurrentConfirmation();
                SceneNavigator.switchTo(DRIVERPENDINGREQUEST_FXML, "Ride confirmation");
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
            SceneNavigator.switchTo(DRIVERPENDINGREQUEST_FXML, "Corsa accettata");
            return;
        }

        // 3. Cerca nuova conferma pending (FIFO)
        Optional<TaxiRideConfirmationBean> confirmation = facade.getNextRideConfirmation(driver.getUserID());

        if (confirmation.isPresent()) {
            DriverSessionContext.getInstance().setCurrentConfirmation(confirmation.get());
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
