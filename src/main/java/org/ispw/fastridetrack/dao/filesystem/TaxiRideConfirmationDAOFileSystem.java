package org.ispw.fastridetrack.dao.filesystem;

import org.ispw.fastridetrack.dao.TaxiRideConfirmationDAO;
import org.ispw.fastridetrack.exception.TaxiRidePersistenceException;
import org.ispw.fastridetrack.model.Client;
import org.ispw.fastridetrack.model.Driver;
import org.ispw.fastridetrack.model.enumeration.RideConfirmationStatus;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.model.TaxiRideConfirmation;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TaxiRideConfirmationDAOFileSystem implements TaxiRideConfirmationDAO {

    private static final String FILE_PATH = "src/data/taxi_rides.csv";
    private final ClientDAOFileSystem clientDAOFileSystem;
    private final DriverDAOFileSystem driverDAOFileSystem;

    public TaxiRideConfirmationDAOFileSystem(ClientDAOFileSystem clientDAOFileSystem, DriverDAOFileSystem driverDAOFileSystem) {
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
            throw new TaxiRidePersistenceException("Impossibile creare file taxi_rides.csv", e);
        }
    }


    @Override
    public void save(TaxiRideConfirmation ride) {
        List<TaxiRideConfirmation> allRides = findAll();
        if (ride.getRideID() == 0) {
            int newId = allRides.stream()
                    .mapToInt(TaxiRideConfirmation::getRideID)
                    .max()
                    .orElse(0) + 1;
            ride.setRideID(newId);
        }
        allRides.add(ride);
        writeAll(allRides);
    }

    @Override
    public Optional<TaxiRideConfirmation> findById(int rideID) {
        return findAll().stream()
                .filter(r -> r.getRideID() == rideID)
                .findFirst();
    }

    @Override
    public void update(TaxiRideConfirmation ride) {
        List<TaxiRideConfirmation> allRides = findAll();
        boolean updated = false;
        for (int i = 0; i < allRides.size(); i++) {
            if (Objects.equals(ride.getRideID(), allRides.get(i).getRideID())) {
                allRides.set(i, ride);
                updated = true;
                break;
            }
        }
        if (!updated) {
            throw new TaxiRidePersistenceException("Nessuna corsa trovata con rideID " + ride.getRideID());
        }
        writeAll(allRides);
    }


    @Override
    public boolean exists(int rideID) {
        return findById(rideID).isPresent();
    }

    @Override
    public Object findByDriverIDandStatus(int driverId, RideConfirmationStatus rideConfirmationStatus) {
        return null;
    }

    private List<TaxiRideConfirmation> findAll() {
        List<TaxiRideConfirmation> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                TaxiRideConfirmation ride = parseLine(line);
                if (ride != null) {
                    list.add(ride);
                }
            }
        } catch (IOException e) {
            throw new TaxiRidePersistenceException("Errore nella lettura del file taxi_rides.csv", e);
        }
        return list;
    }

    private TaxiRideConfirmation parseLine(String line) {
        String[] tokens = line.split(";");
        if (tokens.length < 9) return null;

        try {
            int rideID = Integer.parseInt(tokens[0]);
            int driverID = Integer.parseInt(tokens[1]);
            int clientID = Integer.parseInt(tokens[2]);
            RideConfirmationStatus status = RideConfirmationStatus.valueOf(tokens[3]);
            double estimatedFare = Double.parseDouble(tokens[4]);
            double estimatedTime = Double.parseDouble(tokens[5]);
            PaymentMethod paymentMethod = PaymentMethod.valueOf(tokens[6]);
            LocalDateTime confirmationTime = LocalDateTime.parse(tokens[7]);
            String destination = tokens[8];

            Driver driver = driverDAOFileSystem.findById(driverID);
            Client client = clientDAOFileSystem.findById(clientID);

            if (driver == null || client == null) return null;

            TaxiRideConfirmation ride = new TaxiRideConfirmation();
            ride.setRideID(rideID);
            ride.setDriver(driver);
            ride.setClient(client);
            ride.setStatus(status);
            ride.setEstimatedFare(estimatedFare);
            ride.setEstimatedTime(estimatedTime);
            ride.setPaymentMethod(paymentMethod);
            ride.setConfirmationTime(confirmationTime);
            ride.setDestination(destination);

            return ride;
        } catch (Exception e) {
            // Ignora linee malformate
            return null;
        }
    }

    private void writeAll(List<TaxiRideConfirmation> rides) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (TaxiRideConfirmation r : rides) {
                String line = String.format("%d;%d;%d;%s;%.2f;%.2f;%s;%s;%s",
                        r.getRideID(),
                        r.getDriver().getUserID(),
                        r.getClient().getUserID(),
                        r.getStatus().name(),
                        r.getEstimatedFare(),
                        r.getEstimatedTime(),
                        r.getPaymentMethod().name(),
                        r.getConfirmationTime().toString(),
                        r.getDestination()
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new TaxiRidePersistenceException("Errore nella scrittura del file taxi_rides.csv", e);
        }
    }
}

