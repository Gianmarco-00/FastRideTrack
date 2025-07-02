package org.ispw.fastridetrack.session;

import org.ispw.fastridetrack.dao.*;
import org.ispw.fastridetrack.dao.mysql.*;

public class PersistenceSessionFactory implements SessionFactory {

    private final SingletonDBSession dbSession = SingletonDBSession.getInstance();

    public PersistenceSessionFactory() {
        String usePersistenceEnv = System.getenv("USE_PERSISTENCE");
        if (!"true".equalsIgnoreCase(usePersistenceEnv)) {
            throw new IllegalStateException("PersistenceSessionFactory può essere usata solo con USE_PERSISTENCE=true");
        }
    }

    @Override
    public ClientDAO createClientDAO() {
        return new ClientDAOMYSQL(dbSession.getConnection());
    }

    @Override
    public DriverDAO createDriverDAO() {
        return new DriverDAOMYSQL(dbSession.getConnection());
    }

    @Override
    public RideRequestDAO createRideRequestDAO() {
        ClientDAO clientDAO = createClientDAO();
        return new RideRequestDAOMYSQL(dbSession.getConnection(), clientDAO);
    }

    @Override
    public TaxiRideConfirmationDAO createTaxiRideDAO() {
        return new TaxiRideConfirmationDAOMYSQL(dbSession.getConnection());
    }

    @Override
    public RideDAO createRideDAO() {
        return new RideDAOMYSQL(dbSession.getConnection());
    }
}


