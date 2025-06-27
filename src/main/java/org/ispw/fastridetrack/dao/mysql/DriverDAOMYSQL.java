package org.ispw.fastridetrack.dao.mysql;

import org.ispw.fastridetrack.bean.AvailableDriverBean;
import org.ispw.fastridetrack.bean.DriverBean;
import org.ispw.fastridetrack.dao.DriverDAO;
import org.ispw.fastridetrack.exception.DriverDAOException;
import org.ispw.fastridetrack.model.Coordinate;
import org.ispw.fastridetrack.model.Driver;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DriverDAOMYSQL implements DriverDAO {

    private final Connection connection;
    private static final String GOOGLE_API_KEY = System.getenv("GOOGLE_MAPS_API_KEY"); // Usa la tua chiave
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public DriverDAOMYSQL(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Driver driver) throws DriverDAOException {
        String sql = """
                INSERT INTO driver (
                    userId, username, password, name, email, phonenumber,
                    latitude, longitude, vehicleInfo, vehiclePlate,
                    affiliation, available
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, driver.getUserID());
            stmt.setString(2, driver.getUsername());
            stmt.setString(3, driver.getPassword());
            stmt.setString(4, driver.getName());
            stmt.setString(5, driver.getEmail());
            stmt.setString(6, driver.getPhoneNumber());
            stmt.setDouble(7, driver.getLatitude());
            stmt.setDouble(8, driver.getLongitude());
            stmt.setString(9, driver.getVehicleInfo());
            stmt.setString(10, driver.getVehiclePlate());
            stmt.setString(11, driver.getAffiliation());
            stmt.setInt(12, driver.isAvailable() ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DriverDAOException("Errore SQL durante il salvataggio del driver", e);
        }
    }

    @Override
    public Driver retrieveDriverByUsernameAndPassword(String username, String password) throws DriverDAOException {
        String sql = """
                SELECT userID, username, password, name, email, phoneNumber,
                       latitude, longitude, vehicleInfo, vehiclePlate,
                       affiliation, available
                FROM driver
                WHERE username = ? AND password = ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractDriverFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DriverDAOException("Errore SQL durante il recupero del driver per username e password", e);
        }
        return null;
    }

    @Override
    public Driver findById(int iddriver) throws DriverDAOException {
        String sql = """
                SELECT userID, username, password, name, email, phoneNumber,
                       latitude, longitude, vehicleInfo, vehiclePlate,
                       affiliation, available
                FROM driver
                WHERE userID = ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, iddriver);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractDriverFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DriverDAOException("Errore SQL durante il recupero del driver per ID", e);
        }
        return null;
    }

    @Override
    public List<AvailableDriverBean> findDriversAvailableWithinRadius(Coordinate origin, int radiusKm) throws DriverDAOException {
        List<AvailableDriverBean> availableDrivers = new ArrayList<>();

        String sql = """
                SELECT userID, username, password, name, email, phoneNumber,
                       latitude, longitude, vehicleInfo, vehiclePlate,
                       affiliation, available,
                       (6371 * acos(
                           cos(radians(?)) * cos(radians(latitude)) * cos(radians(longitude) - radians(?)) +
                           sin(radians(?)) * sin(radians(latitude))
                       )) AS distance
                FROM driver
                WHERE available = 1
                  AND latitude BETWEEN ? AND ?
                  AND longitude BETWEEN ? AND ?
                HAVING distance <= ?
                ORDER BY distance ASC
                """;

        double lat = origin.getLatitude();
        double lon = origin.getLongitude();
        double radiusLatDegrees = radiusKm / 111.0;
        double radiusLonDegrees = radiusKm / (111.320 * Math.cos(Math.toRadians(lat)));

        double minLat = lat - radiusLatDegrees;
        double maxLat = lat + radiusLatDegrees;
        double minLon = lon - radiusLonDegrees;
        double maxLon = lon + radiusLonDegrees;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, lat);
            stmt.setDouble(2, lon);
            stmt.setDouble(3, lat);
            stmt.setDouble(4, minLat);
            stmt.setDouble(5, maxLat);
            stmt.setDouble(6, minLon);
            stmt.setDouble(7, maxLon);
            stmt.setDouble(8, radiusKm);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Driver driver = extractDriverFromResultSet(rs);
                    Coordinate destination = new Coordinate(driver.getLatitude(), driver.getLongitude());

                    RouteInfo info = getRouteInfoFromGoogleMaps(origin, destination);
                    double estimatedTime = (info != null) ? info.durationMin : calculateEstimatedTime(origin, destination);
                    double estimatedPrice = (info != null) ? calculateEstimatedPrice(info.distanceKm) : calculateEstimatedPrice(origin, destination);

                    AvailableDriverBean availableDriver = new AvailableDriverBean(
                            DriverBean.fromModel(driver),
                            estimatedTime,
                            estimatedPrice
                    );
                    availableDrivers.add(availableDriver);
                }
            }
        } catch (SQLException e) {
            throw new DriverDAOException("Errore SQL durante la ricerca di driver disponibili nel raggio", e);
        }
        return availableDrivers;
    }


    public void updateAvailability(int driverID, boolean availability) throws DriverDAOException {
        String sql = "UPDATE driver SET available = ? WHERE userID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, availability);
            stmt.setInt(2, driverID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DriverDAOException("Errore SQL durante l'aggiornamento dello stato del driver", e);
        }
    }

    private Driver extractDriverFromResultSet(ResultSet rs) throws SQLException {
        return new Driver(
                rs.getInt("userID"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phoneNumber"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getString("vehicleInfo"),
                rs.getString("vehiclePlate"),
                rs.getString("affiliation"),
                rs.getInt("available") == 1
        );
    }

    private static class RouteInfo {
        double distanceKm;
        double durationMin;

        public RouteInfo(double distanceKm, double durationMin) {
            this.distanceKm = distanceKm;
            this.durationMin = durationMin;
        }
    }

    private RouteInfo getRouteInfoFromGoogleMaps(Coordinate origin, Coordinate dest) {
        try {
            String urlStr = String.format(
                    "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&key=%s",
                    origin.getLatitude(), origin.getLongitude(),
                    dest.getLatitude(), dest.getLongitude(),
                    GOOGLE_API_KEY
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlStr.strip()))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());
            JSONArray routes = json.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONObject leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0);
                double distanceMeters = leg.getJSONObject("distance").getDouble("value");
                double durationSeconds = leg.getJSONObject("duration").getDouble("value");
                return new RouteInfo(distanceMeters / 1000.0, durationSeconds / 60.0);
            }
        } catch (IOException e) {
            System.err.println("Errore IO nel recupero informazioni percorso da Google Maps: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // ripristina lo stato di interruzione
            System.err.println("Thread interrotto durante la richiesta a Google Maps: " + e.getMessage());
        }

        return null;
    }


    private double calculateEstimatedTime(Coordinate origin, Coordinate dest) {
        double distanceKm = calculateDistanceKm(origin.getLatitude(), origin.getLongitude(), dest.getLatitude(), dest.getLongitude());
        double averageSpeedKmPerH = 40.0;
        return (distanceKm / averageSpeedKmPerH) * 60;
    }

    private double calculateEstimatedPrice(Coordinate origin, Coordinate dest) {
        double distanceKm = calculateDistanceKm(origin.getLatitude(), origin.getLongitude(), dest.getLatitude(), dest.getLongitude());
        return calculateEstimatedPrice(distanceKm);
    }

    private double calculateEstimatedPrice(double distanceKm) {
        double baseFare = 3.0;
        double costPerKm = 1.2;
        return baseFare + (costPerKm * distanceKm);
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

}





