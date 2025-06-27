package org.ispw.fastridetrack.dao.mysql;

import org.ispw.fastridetrack.dao.ClientDAO;
import org.ispw.fastridetrack.dao.DriverDAO;
import org.ispw.fastridetrack.dao.RideDAO;
import org.ispw.fastridetrack.exception.DriverDAOException;
import org.ispw.fastridetrack.model.Client;
import org.ispw.fastridetrack.model.Driver;
import org.ispw.fastridetrack.model.Ride;
import org.ispw.fastridetrack.model.enumeration.RideStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class RideDAOMYSQL implements RideDAO {

    private final Connection connection;

    public RideDAOMYSQL(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Ride ride) {
        String sql = "INSERT INTO rides (rideID, driverID, clientID, destination, startTime, endTime, totalPayed, rideStatus)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ride.getRideID());
            stmt.setInt(2, ride.getDriver().getUserID());
            stmt.setInt(3, ride.getClient().getUserID());
            stmt.setString(4, ride.getDestination());
            stmt.setTimestamp(5, Timestamp.valueOf(ride.getStartTime()));
            stmt.setTimestamp(6, null);
            stmt.setBigDecimal(7, null);
            stmt.setString(8, String.valueOf(ride.getStatus()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore salvataggio corsa confermata", e);
        }
    }

    @Override
    public Optional<Ride> findById(int rideID) {
        return Optional.empty();
    }

    @Override
    public void update(Ride ride) {
        String sql = "UPDATE rides SET rideStatus = ?, endTime = ? , totalPayed = ? WHERE rideID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(ride.getStatus()));
            stmt.setTimestamp(2, Timestamp.valueOf(ride.getEndTime()));
            stmt.setBigDecimal(3, ride.getTotalPayed());

            stmt.setInt(4, ride.getRideID());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Nessuna corsa trovata con rideID :" + ride.getRideID());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornamento corsa con rideID " + ride.getRideID(), e);
        }
    }

    @Override
    public boolean exists(int rideID) {
        String sql = "SELECT * FROM rides WHERE rideID = ?";
        return false;
    }

    @Override
    public Optional<Ride> findActiveRideByDriver(int driverId) {
        String sql = "SELECT * FROM rides WHERE driverID = ? AND rideStatus != 'FINISHED';";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, driverId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int rideID = rs.getInt("rideID");
                    int driverID = rs.getInt("driverID");
                    int clientID = rs.getInt("clientID");
                    String status = rs.getString("rideStatus");
                    LocalDateTime startTime = rs.getTimestamp("startTime").toLocalDateTime();
                    String destination = rs.getString("destination");

                    DriverDAO driverDAO = new DriverDAOMYSQL(connection);
                    Driver driver = driverDAO.findById(driverID);

                    ClientDAO clientDAO = new ClientDAOMYSQL(connection);
                    Client client = clientDAO.findById(clientID);

                    Ride model = new Ride();
                    model.setRideID(rideID);
                    model.setDriver(driver);
                    model.setClient(client);
                    model.setStatus(RideStatus.valueOf(status));
                    model.setTotalPayed(null);
                    model.setStartTime(startTime);
                    model.setEndTime(null);
                    model.setDestination(destination);

                    return Optional.of(model);
                } else {
                    return Optional.empty();
                }
            } catch (SQLException | DriverDAOException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
