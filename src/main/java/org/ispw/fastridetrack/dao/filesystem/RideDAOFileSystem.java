package org.ispw.fastridetrack.dao.filesystem;

import org.ispw.fastridetrack.dao.RideDAO;
import org.ispw.fastridetrack.exception.RidePersistenceException;
import org.ispw.fastridetrack.exception.TaxiRidePersistenceException;
import org.ispw.fastridetrack.model.Client;
import org.ispw.fastridetrack.model.Driver;
import org.ispw.fastridetrack.model.Ride;
import org.ispw.fastridetrack.model.enumeration.RideStatus;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RideDAOFileSystem implements RideDAO {

    private static final String FILE_PATH = "src/data/rides.csv";
    private final ClientDAOFileSystem clientDAOFileSystem;
    private final DriverDAOFileSystem driverDAOFileSystem;

    public RideDAOFileSystem(ClientDAOFileSystem clientDAOFileSystem, DriverDAOFileSystem driverDAOFileSystem) {
        this.clientDAOFileSystem = clientDAOFileSystem;
        this.driverDAOFileSystem = driverDAOFileSystem;
        ensureFileExists();
    }

    private void ensureFileExists() {
        try {
            Path path = Paths.get(FILE_PATH);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
        } catch (IOException e) {
            throw new RidePersistenceException("Impossibile creare file rides.csv", e);
        }
    }

    @Override
    public void save(Ride ride) {
        List<Ride> allRides = findAll();
        if (ride.getRideID() == null || ride.getRideID() == 0) {
            int newId = allRides.stream()
                    .mapToInt(Ride::getRideID)
                    .max()
                    .orElse(0) + 1;
            ride.setRideID(newId);
        }
        allRides.add(ride);
        writeAll(allRides);
    }

    @Override
    public Optional<Ride> findById(int rideID) {
        return findAll().stream()
                .filter(r -> r.getRideID() == rideID)
                .findFirst();
    }

    @Override
    public void update(Ride ride) {
        List<Ride> allRides = findAll();
        boolean updated = false;
        for (int i = 0; i < allRides.size(); i++) {
            if (Objects.equals(ride.getRideID(), allRides.get(i).getRideID())) {
                allRides.set(i, ride);
                updated = true;
                break;
            }
        }
        if (!updated) {
            throw new RidePersistenceException("Nessuna corsa trovata con rideID " + ride.getRideID());
        }
        writeAll(allRides);
    }

    @Override
    public boolean exists(int rideID) {
        return findById(rideID).isPresent();
    }

    @Override
    public Optional<Ride> findActiveRideByDriver(int driverID) {
        return findAll().stream()
                .filter(r -> r.getDriver().getUserID() == driverID)
                .filter(r -> r.getStatus() != RideStatus.FINISHED)
                .findFirst();
    }

    private List<Ride> findAll() {
        List<Ride> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                Ride ride = parseLine(line);
                if (ride != null) {
                    list.add(ride);
                }
            }
        } catch (IOException e) {
            throw new TaxiRidePersistenceException("Errore nella lettura del file taxi_rides.csv", e);
        }
        return list;
    }

    private Ride parseLine(String line) {
        String[] tokens = line.split(";");
        if (tokens.length < 9) return null;

        try {
            int rideID = Integer.parseInt(tokens[0]);
            int driverID = Integer.parseInt(tokens[1]);
            int clientID = Integer.parseInt(tokens[2]);
            String destination = tokens[3];
            RideStatus rideStatus = RideStatus.valueOf(tokens[4]);
            LocalDateTime startTime = !"null".equals(tokens[5]) ? LocalDateTime.parse(tokens[5]) : null;
            LocalDateTime endTime = !"null".equals(tokens[6]) ? LocalDateTime.parse(tokens[6]) : null;
            Double totalPayed = Double.valueOf(tokens[7]);
            Boolean clientFetched = Boolean.parseBoolean(tokens[8]);

            Driver driver = driverDAOFileSystem.findById(driverID);
            Client client = clientDAOFileSystem.findById(clientID);
            if (driver == null || client == null) return null;

            Ride ride = new Ride();
            ride.setRideID(rideID);
            ride.setDriver(driver);
            ride.setClient(client);
            ride.setDestination(destination);
            ride.setStatusAndState(rideStatus);
            ride.setStartTime(startTime);
            ride.setEndTime(endTime);
            ride.setTotalPayed(totalPayed);
            ride.setClientFetched(clientFetched);

            return ride;
        } catch (Exception e) {
            return null;
        }
    }

    private void writeAll(List<Ride> rides) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Ride r : rides) {
                String line = String.format("%d;%d;%d;%s;%s;%s;%s;%s;%b",
                        r.getRideID(),
                        r.getDriver().getUserID(),
                        r.getClient().getUserID(),
                        r.getDestination(),
                        r.getStatus().name(),
                        r.getStartTime() != null ? r.getStartTime().toString() : "null",
                        r.getEndTime() != null ? r.getEndTime().toString() : "null",
                        r.getTotalPayed() != null ? r.getTotalPayed().toString() : "0.00",
                        r.isClientFetched()
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RidePersistenceException("Errore nella scrittura del file rides.csv", e);
        }
    }
}
