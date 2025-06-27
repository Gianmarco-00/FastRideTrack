package org.ispw.fastridetrack.dao.mysql;

import org.ispw.fastridetrack.dao.ClientDAO;
import org.ispw.fastridetrack.dao.RideRequestDAO;
import org.ispw.fastridetrack.exception.RideRequestPersistenceException;
import org.ispw.fastridetrack.model.Client;
import org.ispw.fastridetrack.model.RideRequest;

import java.sql.*;

public class RideRequestDAOMYSQL implements RideRequestDAO {
    private final Connection connection;
    private final ClientDAO clientDAO;

    public RideRequestDAOMYSQL(Connection connection, ClientDAO clientDAO) {
        this.connection = connection;
        this.clientDAO = clientDAO;
    }

    @Override
    public RideRequest save(RideRequest request) {
        String query = "INSERT INTO ride_request (clientID, pickupLocation, destination, requestTime) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, request.getClient().getUserID());
            stmt.setString(2, request.getPickupLocation());
            stmt.setString(3, request.getDestination());
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int requestId = rs.getInt(1);
                    request.setRequestId(requestId);
                }
            }
            return request;
        } catch (SQLException e) {
            throw new RideRequestPersistenceException("Errore durante il salvataggio della RideRequest", e);
        }
    }

    @Override
    public RideRequest findById(int requestID) {
        String query = """
            SELECT requestID, clientID, pickupLocation, destination
            FROM ride_request
            WHERE requestID = ?
            """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, requestID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int clientID = rs.getInt("clientID");
                    String pickup = rs.getString("pickupLocation");
                    String destination = rs.getString("destination");

                    Client client = clientDAO.findById(clientID);

                    return new RideRequest(
                            requestID,
                            client,
                            pickup,
                            destination,
                            0,       // radiusKm non gestito
                            null,    // paymentMethod non gestito
                            null     // driver non gestito
                    );
                }
            }
        } catch (SQLException e) {
            throw new RideRequestPersistenceException("Errore durante il recupero della RideRequest", e);
        }
        return null;
    }


    @Override
    public void update(RideRequest request) {
        String query = "UPDATE ride_request SET pickupLocation = ?, destination = ?, clientID = ? WHERE requestID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, request.getPickupLocation());
            stmt.setString(2, request.getDestination());
            stmt.setInt(3, request.getClient().getUserID());
            stmt.setInt(4, request.getRequestId());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RideRequestPersistenceException("Nessun aggiornamento effettuato per requestID " + request.getRequestId());
            }
        } catch (SQLException e) {
            throw new RideRequestPersistenceException("Errore durante l'aggiornamento della RideRequest", e);
        }
    }
}



