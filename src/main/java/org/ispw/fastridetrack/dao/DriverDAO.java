package org.ispw.fastridetrack.dao;

import org.ispw.fastridetrack.bean.AvailableDriverBean;
import org.ispw.fastridetrack.exception.DriverDAOException;
import org.ispw.fastridetrack.model.Coordinate;
import org.ispw.fastridetrack.model.Driver;

import java.util.List;

public interface DriverDAO {
    void save(Driver driver) throws DriverDAOException;
    Driver findById(int iddriver) throws DriverDAOException;
    Driver retrieveDriverByUsernameAndPassword(String username, String password) throws DriverDAOException;
    List<AvailableDriverBean> findDriversAvailableWithinRadius(Coordinate origin, int radiusKm) throws DriverDAOException;
    void updateAvailability(int driverId, boolean isAvailable) throws DriverDAOException;
}



