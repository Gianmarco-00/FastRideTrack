package org.ispw.fastridetrack.controller.applicationcontroller;

import org.ispw.fastridetrack.bean.TaxiRideConfirmationBean;
import org.ispw.fastridetrack.dao.DriverDAO;
import org.ispw.fastridetrack.dao.TaxiRideConfirmationDAO;
import org.ispw.fastridetrack.exception.*;
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

    private List<TaxiRideConfirmation> getPendingConfirmationsForDriver(int driverId) throws RideConfirmationNotFoundException {
        List<TaxiRideConfirmation> requests = taxiRideConfirmationDAO.findByDriverIDandStatus(driverId, RideConfirmationStatus.PENDING);
        requests.sort(Comparator.comparing(TaxiRideConfirmation::getConfirmationTime));
        return requests;
    }

    public Optional<TaxiRideConfirmationBean> getNextRideConfirmation(int driverId) throws RideConfirmationNotFoundException, DriverDAOException {
        if(!checkDriverAvailability(driverId)){
            throw new DriverUnavailableException("Driver is unavailable");
        }
        List<TaxiRideConfirmation> requests = getPendingConfirmationsForDriver(driverId);
        if (!requests.isEmpty()) {
            return Optional.of(TaxiRideConfirmationBean.fromModel(requests.getFirst()));
        } else {
            return Optional.empty();
        }
    }

    private boolean checkDriverAvailability(int driverID) throws DriverDAOException {
        return driverDAO.findById(driverID).isAvailable();
    }

    public boolean checkRideConfirmationStillPending(int rideID) throws RideConfirmationNotFoundException {
        return taxiRideConfirmationDAO.findById(rideID)
                .map(conf -> conf.getStatus() == RideConfirmationStatus.PENDING)
                .orElseThrow(() -> new RideConfirmationNotFoundException(rideID));
    }

    public TaxiRideConfirmationBean acceptRideConfirmationAndRejectOthers(int rideId, int driverId) throws DriverDAOException {
        if(!checkDriverAvailability(driverId)){
            throw new DriverUnavailableException("Driver is unavailable");
        }

        TaxiRideConfirmation accepted = taxiRideConfirmationDAO.findById(rideId)
                .orElseThrow(() -> new RideConfirmationNotFoundException(rideId));

        if (accepted.getStatus() != RideConfirmationStatus.PENDING) {
            throw new RideConfirmationNotPendingException("No ride confirmation found with ID: " + rideId);
        }

        if (!accepted.getDriver().getUserID().equals(driverId)) {
            throw new DriverMismatchException("Driver mismatch for confirmation ID: " + rideId);
        }

        accepted.setStatus(RideConfirmationStatus.ACCEPTED);
        taxiRideConfirmationDAO.update(accepted);

        List<TaxiRideConfirmation> others = taxiRideConfirmationDAO.findByDriverIDandStatus(driverId, RideConfirmationStatus.PENDING);
        for (TaxiRideConfirmation other : others) {
            if (!other.getRideID().equals(rideId)) {
                other.setStatus(RideConfirmationStatus.REJECTED);
                taxiRideConfirmationDAO.update(other);
            }
        }

        driverDAO.updateAvailability(driverId, false);
        SessionManager.getInstance().getLoggedDriver().setAvailable(false);

        return TaxiRideConfirmationBean.fromModel(accepted);
    }

    public void rejectRideConfirmation(int rideId, int driverId) {
        Optional<TaxiRideConfirmation> request = taxiRideConfirmationDAO.findById(rideId);

        if (request.isPresent() && request.get().getDriver().getUserID().equals(driverId)) {
            request.get().setStatus(RideConfirmationStatus.REJECTED);
            taxiRideConfirmationDAO.update(request.get());
        }
    }
}
