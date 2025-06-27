package org.ispw.fastridetrack.controller.applicationcontroller;

import org.ispw.fastridetrack.bean.TaxiRideConfirmationBean;
import org.ispw.fastridetrack.dao.DriverDAO;
import org.ispw.fastridetrack.dao.TaxiRideConfirmationDAO;
import org.ispw.fastridetrack.exception.DriverDAOException;
import org.ispw.fastridetrack.exception.RideConfirmationNotFoundException;
import org.ispw.fastridetrack.model.Driver;
import org.ispw.fastridetrack.model.TaxiRideConfirmation;
import org.ispw.fastridetrack.session.SessionManager;
import org.ispw.fastridetrack.model.enumeration.RideConfirmationStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RideConfirmationApplicationController {
    private final TaxiRideConfirmationDAO taxiRideConfirmationDAO;
    private final DriverDAO driverDAO;

    public RideConfirmationApplicationController() {
        this.taxiRideConfirmationDAO = SessionManager.getInstance().getTaxiRideDAO();
        this.driverDAO = SessionManager.getInstance().getDriverDAO();
    }

    /**
     * Restituisce tutte le richieste ride ordinate per orario di creazione.
     */
    public List<TaxiRideConfirmation> getPendingConfirmationsForDriver(int driverId) throws RideConfirmationNotFoundException {
        List<TaxiRideConfirmation> requests = (List<TaxiRideConfirmation>) taxiRideConfirmationDAO.findByDriverIDandStatus(driverId, RideConfirmationStatus.PENDING);
        requests.sort(Comparator.comparing(TaxiRideConfirmation::getConfirmationTime));
        return requests;
    }

    public Optional<TaxiRideConfirmationBean> getNextRideConfirmation(int driverId) throws RideConfirmationNotFoundException{
        List<TaxiRideConfirmation> requests = getPendingConfirmationsForDriver(driverId);
        if (!requests.isEmpty()) {
            return Optional.of(TaxiRideConfirmationBean.fromModel(requests.getFirst()));  // prima richiesta in ordine FIFO
        } else {
            return Optional.empty();
        }
    }

    /**
     * Accetta una richiesta specifica e rifiuta tutte le altre associate al driver.
     */
    public void acceptRideConfirmationAndRejectOthers(int rideId, int driverId) throws DriverDAOException {
        //SessionDataApplicationController sessionC = new SessionDataApplicationController();
        Driver driver = SessionManager.getInstance().getLoggedDriver();

        Optional<TaxiRideConfirmation> accepted = taxiRideConfirmationDAO.findById(rideId);

        if (accepted.isPresent() && accepted.get().getDriver().getUserID().equals(driverId)) {
            accepted.get().setStatus(RideConfirmationStatus.ACCEPTED);
            accepted.ifPresent(taxiRideConfirmationDAO::update);
            SessionManager.getInstance().setCurrentConfirmation(accepted.get());
            driver.setAvailable(false);
            driverDAO.updateAvailability(driverId, false);
            SessionManager.getInstance().setLoggedDriver(driver);

            List<TaxiRideConfirmation> others = (List<TaxiRideConfirmation>) taxiRideConfirmationDAO.findByDriverIDandStatus(driverId, RideConfirmationStatus.PENDING);
            for (TaxiRideConfirmation r : others) {
                if (!r.getRideID().equals(rideId)) {
                    r.setStatus(RideConfirmationStatus.REJECTED);
                    taxiRideConfirmationDAO.update(r);
                }
            }
        }
    }

    /**
     * Rifiuta una richiesta specifica di conferma corsa.
     */
    public void rejectRideConfirmation(int rideId, int driverId) {
        Optional<TaxiRideConfirmation> request = taxiRideConfirmationDAO.findById(rideId);

        if (request.isPresent() && request.get().getDriver().getUserID().equals(driverId)) {
            request.get().setStatus(RideConfirmationStatus.REJECTED);
            taxiRideConfirmationDAO.update(request.get());
        }
    }
}
