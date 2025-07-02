package org.ispw.fastridetrack.controller.applicationcontroller;

import org.ispw.fastridetrack.bean.LocationBean;
import org.ispw.fastridetrack.bean.RideBean;
import org.ispw.fastridetrack.bean.TaxiRideConfirmationBean;
import org.ispw.fastridetrack.dao.RideDAO;
import org.ispw.fastridetrack.exception.ClientNotFetchedException;
import org.ispw.fastridetrack.exception.RideAlreadyActiveException;
import org.ispw.fastridetrack.model.Location;
import org.ispw.fastridetrack.session.SessionManager;
import org.ispw.fastridetrack.model.Ride;
import org.ispw.fastridetrack.model.enumeration.RideStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public class CurrentRideManagementApplicationController {

    private final RideDAO rideDAO;

    public CurrentRideManagementApplicationController() {
        this.rideDAO = SessionManager.getInstance().getRideDAO();
    }

    private boolean checkRideAlreadyActiveForDriver(int driverID) {
        return rideDAO.findActiveRideByDriver(driverID).isPresent();
    }

    public RideBean getCurrentActiveRideByDriver(int driverID){
        Optional<Ride> existingRide = rideDAO.findActiveRideByDriver(driverID);
        if (existingRide.isPresent()) {
            Ride ride = existingRide.get();
            return RideBean.fromModel(ride);
        }
        return null;
    }

    public RideBean initializeCurrentRide(TaxiRideConfirmationBean confirmationBean) throws RideAlreadyActiveException {
        if(checkRideAlreadyActiveForDriver(confirmationBean.getDriver().getUserID())){
            throw new RideAlreadyActiveException("Ride already active for driver: " + confirmationBean.getDriver().getUserID());
        }
        Ride ride = new Ride(
                confirmationBean.getRideID(),
                confirmationBean.getClient().toModel(),
                confirmationBean.getDriver().toModel(),
                confirmationBean.getDestination(),
                LocalDateTime.now(),
                null,
                null,
                false,
                RideStatus.INITIATED
        );
        rideDAO.save(ride);
        return RideBean.fromModel(ride);
    }

    public RideBean markClientLocated(RideBean rideBean) {
        Ride ride = rideDAO.findById(rideBean.getRideID())
                .orElseThrow(() -> new IllegalStateException("The current ride does not exist."));

        ride.markClientFound();
        rideDAO.update(ride);
        return RideBean.fromModel(ride);
    }

    public RideBean startRide(RideBean rideBean) throws ClientNotFetchedException {
        Ride ride = rideDAO.findById(rideBean.getRideID())
                .orElseThrow(() -> new IllegalStateException("current ride does not exist."));

        if (!ride.isClientFetched()) {
            throw new ClientNotFetchedException("Client not yet fetched.");
        }

        ride.startRide();
        rideDAO.update(ride);
        return RideBean.fromModel(ride);
    }

    public RideBean finishRide(RideBean rideBean, Double totalFare) {
        Ride ride = rideDAO.findById(rideBean.getRideID())
                .orElseThrow(() -> new IllegalStateException("current ride does not exist."));

        ride.finishRide(totalFare);
        rideDAO.update(ride);
        return RideBean.fromModel(ride);
    }

    public LocationBean getCurrentMapStartPoint(RideBean rideBean) {
        Ride ride = rideDAO.findById(rideBean.getRideID())
                .orElseThrow(() -> new IllegalStateException("ride does not exist."));
        Location startpoint = ride.getMapStartPoint();
        return LocationBean.fromModel(startpoint);
    }

    public LocationBean getCurrentMapEndPoint(RideBean rideBean) {
        Ride ride = rideDAO.findById(rideBean.getRideID())
                .orElseThrow(() -> new IllegalStateException("ride does not exist."));
        Location endpoint = ride.getMapEndPoint();
        return LocationBean.fromModel(endpoint);
    }

}
