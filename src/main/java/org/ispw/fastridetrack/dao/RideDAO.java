package org.ispw.fastridetrack.dao;


import org.ispw.fastridetrack.model.Ride;

import java.util.Optional;

public interface RideDAO {
    void save(Ride ride);
    Optional<Ride> findById(int rideID);
    void update(Ride ride);
    boolean exists(int rideID);
    Optional<Ride> findActiveRideByDriver(int driverID);
}
