package org.ispw.fastridetrack.dao.filesystem;

import org.ispw.fastridetrack.bean.AvailableDriverBean;
import org.ispw.fastridetrack.bean.DriverBean;
import org.ispw.fastridetrack.dao.DriverDAO;
import org.ispw.fastridetrack.exception.DriverDAOException;
import org.ispw.fastridetrack.model.Coordinate;
import org.ispw.fastridetrack.model.Driver;

import java.io.*;
import java.util.*;

@SuppressWarnings("java:S6213")
public class DriverDAOFileSystem implements DriverDAO {
    private static final String FILE_PATH = "src/data/drivers.csv";

    public DriverDAOFileSystem() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                File parent = file.getParentFile();
                if (parent != null) {
                    parent.mkdirs(); // crea la cartella "data"
                }
                boolean created = file.createNewFile();
                if (!created) {
                    System.err.println("Il file driver CSV NON è stato creato perché esiste già o errore sconosciuto.");
                }
            } catch (IOException e) {
                System.err.println("Errore nella creazione del file driver CSV: " + e.getMessage());
            }
        }
    }


    @Override
    public void save(Driver driver) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            String record = String.join(";",
                    String.valueOf(driver.getUserID()),
                    driver.getUsername(),
                    driver.getPassword(),
                    driver.getName(),
                    driver.getEmail(),
                    driver.getPhoneNumber(),
                    String.valueOf(driver.getLatitude()),
                    String.valueOf(driver.getLongitude()),
                    driver.getVehicleInfo(),
                    driver.getVehiclePlate(),
                    driver.getAffiliation(),
                    driver.isAvailable() ? "1" : "0"
            );
            writer.write(record);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio del driver: " + e.getMessage());
        }
    }

    @Override
    public Driver retrieveDriverByUsernameAndPassword(String username, String password) {
        return getAllDrivers().stream()
                .filter(d -> d.getUsername().equals(username) && d.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Driver findById(int iddriver) {
        return getAllDrivers().stream()
                .filter(d -> d.getUserID() == iddriver)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<AvailableDriverBean> findDriversAvailableWithinRadius(Coordinate origin, int radiusKm) {
        List<AvailableDriverBean> available = new ArrayList<>();
        for (Driver driver : getAllDrivers()) {
            if (!driver.isAvailable()) continue;
            Coordinate dest = new Coordinate(driver.getLatitude(), driver.getLongitude());
            double distance = calculateDistanceKm(origin, dest);
            if (distance <= radiusKm) {
                double estimatedTime = (distance / 40.0) * 60; // media 40km/h
                double estimatedPrice = 3.0 + distance * 1.2;
                available.add(new AvailableDriverBean(DriverBean.fromModel(driver), estimatedTime, estimatedPrice));
            }
        }
        return available;
    }

    @Override
    public void updateAvailability(int driverId, boolean isAvailable) throws DriverDAOException {
        List<Driver> allDrivers = getAllDrivers();
        boolean updated = false;

        for (Driver driver : allDrivers) {
            if (driver.getUserID() == driverId) {
                driver.setAvailable(isAvailable);
                updated = true;
                break;
            }
        }

        if (!updated) {
            throw new DriverDAOException("Driver con ID " + driverId + " non trovato.");
        }

        // Sovrascrive tutto il file con la nuova lista aggiornata
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Driver driver : allDrivers) {
                String record = String.join(";",
                        String.valueOf(driver.getUserID()),
                        driver.getUsername(),
                        driver.getPassword(),
                        driver.getName(),
                        driver.getEmail(),
                        driver.getPhoneNumber(),
                        String.valueOf(driver.getLatitude()),
                        String.valueOf(driver.getLongitude()),
                        driver.getVehicleInfo(),
                        driver.getVehiclePlate(),
                        driver.getAffiliation(),
                        driver.isAvailable() ? "1" : "0"
                );
                writer.write(record);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new DriverDAOException("Errore durante la scrittura del file driver.csv", e);
        }
    }

    private List<Driver> getAllDrivers() {
        List<Driver> drivers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(";", -1);
                if (tokens.length < 12) continue;
                Driver d = new Driver(
                        Integer.parseInt(tokens[0]), tokens[1], tokens[2], tokens[3],
                        tokens[4], tokens[5],
                        Double.parseDouble(tokens[6]), Double.parseDouble(tokens[7]),
                        tokens[8], tokens[9], tokens[10],
                        tokens[11].equals("1")
                );
                drivers.add(d);
            }
        } catch (IOException e) {
            System.err.println("Errore nella lettura dei driver da file: " + e.getMessage());
        }
        return drivers;
    }

    private double calculateDistanceKm(Coordinate c1, Coordinate c2) {
        final int R = 6371;
        double latDistance = Math.toRadians(c2.getLatitude() - c1.getLatitude());
        double lonDistance = Math.toRadians(c2.getLongitude() - c1.getLongitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(c1.getLatitude())) * Math.cos(Math.toRadians(c2.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

}

