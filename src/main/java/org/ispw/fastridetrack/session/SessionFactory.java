package org.ispw.fastridetrack.session;

import org.ispw.fastridetrack.dao.*;

public interface SessionFactory {
    ClientDAO createClientDAO();
    DriverDAO createDriverDAO();
    RideRequestDAO createRideRequestDAO();
    TaxiRideConfirmationDAO createTaxiRideDAO();
    RideDAO createRideDAO();
}