package org.ispw.fastridetrack.dao.inmemory;

import org.ispw.fastridetrack.dao.TaxiRideConfirmationDAO;
import org.ispw.fastridetrack.exception.RideNotFoundException;
import org.ispw.fastridetrack.model.TaxiRideConfirmation;
import org.ispw.fastridetrack.model.enumeration.RideConfirmationStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TaxiRideConfirmationDAOInMemory implements TaxiRideConfirmationDAO {
    private final Map<Integer, TaxiRideConfirmation> rides = new HashMap<>();

    @Override
    public void save(TaxiRideConfirmation ride) {
        rides.put(ride.getRideID(), ride);
    }

    @Override
    public Optional<TaxiRideConfirmation> findById(int rideID) {
        return Optional.ofNullable(rides.get(rideID));
    }

    @Override
    public void update(TaxiRideConfirmation updatedRide) {
        int rideID = updatedRide.getRideID();
        if (!rides.containsKey(rideID)) {
            throw new RideNotFoundException(rideID);
        }
        rides.put(rideID, updatedRide);
    }

    @Override
    public boolean exists(int rideID) {
        return rides.containsKey(rideID);
    }

    @Override
    public Object findByDriverIDandStatus(int driverId, RideConfirmationStatus rideConfirmationStatus) {
        return null;
    }
}


