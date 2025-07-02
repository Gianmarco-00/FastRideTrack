package org.ispw.fastridetrack.session;

import org.ispw.fastridetrack.dao.*;
import org.ispw.fastridetrack.dao.filesystem.*;

public class FileSystemSessionFactory implements SessionFactory {

    private final ClientDAOFileSystem clientDAO = new ClientDAOFileSystem();
    private final DriverDAOFileSystem driverDAO = new DriverDAOFileSystem();

    public FileSystemSessionFactory() {
        String mode = System.getenv("USE_PERSISTENCE");
        if (!"file".equalsIgnoreCase(mode)) {
            throw new IllegalStateException("FileSystemSessionFactory può essere usata solo con USE_PERSISTENCE=file");
        }
    }

    @Override
    public ClientDAO createClientDAO() {
        return clientDAO;
    }

    @Override
    public DriverDAO createDriverDAO() {
        return driverDAO;
    }

    @Override
    public RideRequestDAO createRideRequestDAO() {
        return new RideRequestDAOFileSystem(clientDAO);
    }

    @Override
    public TaxiRideConfirmationDAO createTaxiRideDAO() {
        return new TaxiRideConfirmationDAOFileSystem(clientDAO, driverDAO);
    }

    @Override
    public RideDAO createRideDAO() {
        return new RideDAOFileSystem(clientDAO, driverDAO);
    }
}


