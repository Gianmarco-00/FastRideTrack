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

//Nota: Optional<TaxiRideConfirmation> indica un ritorno facoltativo,
//      ovvero un valore che potrebbe esserci o no, e che va gestito esplicitamente
//      per evitare errori legati a null.
