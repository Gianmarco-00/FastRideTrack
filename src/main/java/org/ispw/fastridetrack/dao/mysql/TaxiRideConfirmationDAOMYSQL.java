package org.ispw.fastridetrack.dao.mysql;

import org.ispw.fastridetrack.dao.ClientDAO;
import org.ispw.fastridetrack.dao.DriverDAO;
import org.ispw.fastridetrack.dao.TaxiRideConfirmationDAO;
import org.ispw.fastridetrack.exception.DriverDAOException;
import org.ispw.fastridetrack.exception.TaxiRidePersistenceException;
import org.ispw.fastridetrack.exception.TaxiRideRetrievalException;
import org.ispw.fastridetrack.model.*;
import org.ispw.fastridetrack.model.Driver;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.model.enumeration.RideConfirmationStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaxiRideConfirmationDAOMYSQL implements TaxiRideConfirmationDAO {

    private final Connection connection;

    public TaxiRideConfirmationDAOMYSQL(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(TaxiRideConfirmation ride) {
        String sql = "INSERT INTO taxi_rides (rideID, driverID, clientID, rideConfirmationStatus, estimatedFare, estimatedTime, paymentMethod, confirmationTime, destination) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ride.getRideID());
            stmt.setInt(2, ride.getDriver().getUserID());
            stmt.setInt(3, ride.getClient().getUserID());
            stmt.setString(4, String.valueOf(ride.getStatus()));
            stmt.setDouble(5, ride.getEstimatedFare());
            stmt.setDouble(6, ride.getEstimatedTime());
            stmt.setString(7, String.valueOf(ride.getPaymentMethod()));
            stmt.setTimestamp(8, Timestamp.valueOf(ride.getConfirmationTime()));

            // Campo destination (string)
            stmt.setString(9, ride.getDestination());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new TaxiRidePersistenceException("Errore salvataggio corsa confermata", e);
        }
    }

    @Override
    public Optional<TaxiRideConfirmation> findById(int rideID) {
        String sql = """
        SELECT rideID, driverID, clientID, rideConfirmationStatus, estimatedFare,
               estimatedTime, paymentMethod, confirmationTime, destination
        FROM taxi_rides
        WHERE rideID = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, rideID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int driverID = rs.getInt("driverID");
                    int clientID = rs.getInt("clientID");
                    String status = rs.getString("rideConfirmationStatus");
                    double estimatedFare = rs.getDouble("estimatedFare");
                    double estimatedTime = rs.getDouble("estimatedTime");
                    String paymentMethod = rs.getString("paymentMethod");
                    Timestamp confirmationTimestamp = rs.getTimestamp("confirmationTime");
                    String destination = rs.getString("destination");

                    DriverDAO driverDAO = new DriverDAOMYSQL(connection);
                    Driver driver = driverDAO.findById(driverID);

                    ClientDAO clientDAO = new ClientDAOMYSQL(connection);
                    Client client = clientDAO.findById(clientID);

                    TaxiRideConfirmation model = new TaxiRideConfirmation();
                    model.setRideID(rideID);
                    model.setDriver(driver);
                    model.setClient(client);
                    model.setStatus(RideConfirmationStatus.valueOf(status));
                    model.setEstimatedFare(estimatedFare);
                    model.setEstimatedTime(estimatedTime);
                    model.setPaymentMethod(PaymentMethod.valueOf(paymentMethod));
                    model.setConfirmationTime(confirmationTimestamp.toLocalDateTime());
                    model.setDestination(destination);

                    return Optional.of(model);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException | DriverDAOException e) {
            throw new TaxiRidePersistenceException("Errore nel recupero di TaxiRide con rideID " + rideID, e);
        }
    }


    @Override
    public void update(TaxiRideConfirmation ride) {
        String sql = "UPDATE taxi_rides SET driverID = ?, clientID = ?, rideConfirmationStatus = ?, estimatedFare = ?, estimatedTime = ?, paymentMethod = ?, confirmationTime = ?, destination = ? WHERE rideID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ride.getDriver().getUserID());
            stmt.setInt(2, ride.getClient().getUserID());
            stmt.setString(3, String.valueOf(ride.getStatus()));
            stmt.setDouble(4, ride.getEstimatedFare());
            stmt.setDouble(5, ride.getEstimatedTime());
            stmt.setString(6, String.valueOf(ride.getPaymentMethod()));
            stmt.setTimestamp(7, Timestamp.valueOf(ride.getConfirmationTime()));
            stmt.setString(8, ride.getDestination());

            stmt.setInt(9, ride.getRideID());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new TaxiRidePersistenceException("Nessuna corsa trovata con rideID " + ride.getRideID());
            }
        } catch (SQLException e) {
            throw new TaxiRidePersistenceException("Errore aggiornamento corsa con rideID " + ride.getRideID(), e);
        }
    }


    @Override
    public boolean exists(int rideID) {
        String sql = "SELECT 1 FROM taxi_rides WHERE rideID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, rideID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new TaxiRidePersistenceException("Errore durante il controllo esistenza della corsa con rideID " + rideID, e);
        }
    }

    @Override
    public List<TaxiRideConfirmation> findByDriverIDandStatus(int driverID, RideConfirmationStatus status) {
        String sql = """
        SELECT rideID, clientID, driverID, destination, rideConfirmationStatus,
               confirmationTime, estimatedFare, estimatedTime, paymentMethod
        FROM taxi_rides
        WHERE driverID = ? AND rideConfirmationStatus = ?
        ORDER BY confirmationTime ASC
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, driverID);
            stmt.setString(2, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                return extractRideRequestsFromResultSet(rs);
            }
        } catch (SQLException | DriverDAOException e) {
            throw new TaxiRideRetrievalException("Errore nel recupero delle ride request per driverID=" + driverID + " e status=" + status, e);
        }
    }

    private List<TaxiRideConfirmation> extractRideRequestsFromResultSet(ResultSet rs) throws SQLException, DriverDAOException {
        List<TaxiRideConfirmation> requests = new ArrayList<>();
        ClientDAO clientDAO = new ClientDAOMYSQL(connection);
        DriverDAO driverDAO = new DriverDAOMYSQL(connection);

        while (rs.next()) {
            TaxiRideConfirmation request = new TaxiRideConfirmation();
            request.setRideID(rs.getInt("rideID"));
            request.setClient(clientDAO.findById(rs.getInt("clientID")));
            request.setDriver(driverDAO.findById(rs.getInt("driverID")));
            request.setDestination(rs.getString("destination"));
            request.setStatus(RideConfirmationStatus.valueOf(rs.getString("rideConfirmationStatus")));
            request.setConfirmationTime(rs.getTimestamp("confirmationTime").toLocalDateTime());
            request.setEstimatedFare(rs.getDouble("estimatedFare"));
            request.setEstimatedTime(rs.getDouble("estimatedTime"));
            request.setPaymentMethod(PaymentMethod.valueOf(rs.getString("paymentMethod")));
            requests.add(request);
        }
        return requests;
    }


}



