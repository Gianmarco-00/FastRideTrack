package org.ispw.fastridetrack.session;

import org.ispw.fastridetrack.dao.*;
import org.ispw.fastridetrack.dao.inmemory.*;

public class InMemorySessionFactory implements SessionFactory {

    @Override
    public ClientDAO createClientDAO() {
        return new ClientDAOInMemory();
    }

    @Override
    public DriverDAO createDriverDAO() {
        return new DriverDAOInMemory();
    }

    @Override
    public RideRequestDAO createRideRequestDAO() {
        return new RideRequestDAOInMemory();
    }

    @Override
    public TaxiRideConfirmationDAO createTaxiRideDAO() {
        return new TaxiRideConfirmationDAOInMemory();
    }

    @Override
    public RideDAO createRideDAO() {
        return new RideDAOInMemory();
    }
}

