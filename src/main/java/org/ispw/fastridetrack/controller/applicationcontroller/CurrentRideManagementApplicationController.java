package org.ispw.fastridetrack.controller.applicationcontroller;

import org.ispw.fastridetrack.bean.TaxiRideConfirmationBean;
import org.ispw.fastridetrack.dao.RideDAO;
import org.ispw.fastridetrack.model.Ride;
import org.ispw.fastridetrack.model.TaxiRideConfirmation;
import org.ispw.fastridetrack.session.SessionManager;
import org.ispw.fastridetrack.model.enumeration.RideStatus;

import java.time.LocalDateTime;

public class CurrentRideManagementApplicationController {

    private final RideDAO rideDAO;

    public CurrentRideManagementApplicationController() {
        this.rideDAO = SessionManager.getInstance().getRideDAO();
    }

    public void initializeCurrentRide(TaxiRideConfirmationBean confirmationBean) {
        TaxiRideConfirmation confirmation  = confirmationBean.toModel();
        Ride ride = new Ride(
                confirmation.getRideID(),
                confirmation.getClient(),
                confirmation.getDriver(),
                confirmation.getDestination(),
                LocalDateTime.now(),
                null,
                null,
                RideStatus.INITIATED
        );
        SessionManager.getInstance().setCurrentRide(ride);
        rideDAO.save(ride);
    }

    public void confirmClientLocated() {
        Ride ride = getCurrentRideOrFail();
        if (ride.getStatus() != RideStatus.INITIATED) {
            throw new IllegalStateException("Il cliente non può essere localizzato. ");
        }
        ride.setStatus(RideStatus.CLIENT_LOCATED);
        rideDAO.update(ride);
    }

    public void startRide() {
        Ride ride = getCurrentRideOrFail();
        if (ride.getStatus() != RideStatus.CLIENT_LOCATED) {
            throw new IllegalStateException("La corsa può iniziare solo se il cliente è stato localizzato.");
        }
        ride.setStatus(RideStatus.ONGOING);
        ride.setStartTime(LocalDateTime.now());
        rideDAO.update(ride);
    }

    public void finishRide() {
        Ride ride = getCurrentRideOrFail();
        if (ride.getStatus() != RideStatus.ONGOING) {
            throw new IllegalStateException("La corsa può essere completata solo se è in corso.");
        }
        ride.setStatus(RideStatus.FINISHED);
        ride.setEndTime(LocalDateTime.now());
        //ride.setTotalFare(calculateFare(ride));
        rideDAO.update(ride);
    }

    private Ride getCurrentRideOrFail() throws IllegalStateException {
        Ride ride = SessionManager.getInstance().getDriverSessionContext().getCurrentRide();
        if (ride == null) {
            throw new IllegalStateException("Nessuna corsa attiva nella sessione.");
        }
        return ride;
    }

    private double calculateFare(Ride ride) {
        // Logica di esempio
        return 15.0;
    }
}
