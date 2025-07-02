package org.ispw.fastridetrack.dao.inmemory;

import org.ispw.fastridetrack.dao.RideDAO;
import org.ispw.fastridetrack.exception.RideNotFoundException;
import org.ispw.fastridetrack.model.Ride;
import org.ispw.fastridetrack.model.enumeration.RideStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RideDAOInMemory implements RideDAO {

    private final Map<Integer, Ride> rides = new HashMap<>();
    private int nextId = 1;

    @Override
    public void save(Ride ride) {
        if (ride.getRideID() == null || ride.getRideID() == 0) {
            ride.setRideID(nextId++);
        }
        rides.put(ride.getRideID(), ride);
    }

    @Override
    public Optional<Ride> findById(int rideID) {
        return Optional.ofNullable(rides.get(rideID));
    }

    @Override
    public void update(Ride ride) {
        Integer id = ride.getRideID();
        if (id == null || !rides.containsKey(id)) {
            throw new RideNotFoundException(ride.getRideID());
        }
        rides.put(id, ride);
    }

    @Override
    public boolean exists(int rideID) {
        return rides.containsKey(rideID);
    }

    @Override
    public Optional<Ride> findActiveRideByDriver(int driverID) {
        return rides.values().stream()
                .filter(ride -> ride.getDriver().getUserID() == driverID)
                .filter(ride -> ride.getStatus() != RideStatus.FINISHED)
                .findFirst();
    }
}
