package org.ispw.fastridetrack.dao;

import org.ispw.fastridetrack.model.TaxiRideConfirmation;
import org.ispw.fastridetrack.model.enumeration.RideConfirmationStatus;

import java.util.Optional;

public interface TaxiRideConfirmationDAO {
    void save(TaxiRideConfirmation ride);
    Optional<TaxiRideConfirmation> findById(int rideID);
    void update(TaxiRideConfirmation ride);
    boolean exists(int rideID);
    Object findByDriverIDandStatus(int driverId, RideConfirmationStatus rideConfirmationStatus);
}


