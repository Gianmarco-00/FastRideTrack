package org.ispw.fastridetrack.dao.filesystem;

import org.ispw.fastridetrack.dao.RideRequestDAO;
import org.ispw.fastridetrack.exception.RideRequestPersistenceException;
import org.ispw.fastridetrack.model.Client;
import org.ispw.fastridetrack.model.RideRequest;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RideRequestDAOFileSystem implements RideRequestDAO {

    private static final String FILE_PATH = "src/data/ride_requests.csv";
    private final ClientDAOFileSystem clientDAOFileSystem;

    public RideRequestDAOFileSystem(ClientDAOFileSystem clientDAOFileSystem) {
        this.clientDAOFileSystem = clientDAOFileSystem;
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
            throw new RideRequestPersistenceException("Impossibile creare file ride_requests.csv", e);
        }
    }

    @Override
    public RideRequest save(RideRequest request) {
        List<RideRequest> allRequests = findAll();
        int newId = allRequests.stream()
                .map(RideRequest::getRequestId)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1;
        request.setRequestId(newId);
        allRequests.add(request);
        writeAll(allRequests);
        return request;
    }


    @Override
    public RideRequest findById(int requestID) {
        return findAll().stream()
                .filter(r -> r.getRequestId().equals(requestID))
                .findFirst()
                .orElse(null);
    }


    @Override
    public void update(RideRequest request) {
        List<RideRequest> allRequests = findAll();
        boolean updated = false;
        for (int i = 0; i < allRequests.size(); i++) {
            if (allRequests.get(i).getRequestId().equals(request.getRequestId())) {
                allRequests.set(i, request);
                updated = true;
                break;
            }
        }
        if (!updated) {
            throw new RideRequestPersistenceException("Nessuna RideRequest trovata con ID " + request.getRequestId());
        }
        writeAll(allRequests);
    }


    private List<RideRequest> findAll() {
        List<RideRequest> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                RideRequest r = parseLine(line);
                if (r != null) {
                    list.add(r);
                }
            }
        } catch (IOException e) {
            throw new RideRequestPersistenceException("Errore nella lettura del file ride_requests.csv", e);
        }
        return list;
    }

    private RideRequest parseLine(String line) {
        String[] tokens = line.split(";");
        if (tokens.length < 4) return null;

        try {
            int requestId = Integer.parseInt(tokens[0]);
            int clientId = Integer.parseInt(tokens[1]);
            String pickup = tokens[2];
            String destination = tokens[3];

            Client client = clientDAOFileSystem.findById(clientId);
            if (client == null) {
                // Ignora se client non trovato
                return null;
            }

            return new RideRequest(requestId, client, pickup, destination, 0, null, null);
        } catch (Exception e) {
            // Ignora linee malformate
            return null;
        }
    }

    private void writeAll(List<RideRequest> requests) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (RideRequest r : requests) {
                // Scrive in formato CSV: requestId;clientID;pickupLocation;destination
                String line = String.format("%d;%d;%s;%s",
                        r.getRequestId(),
                        r.getClient().getUserID(),
                        r.getPickupLocation(),
                        r.getDestination());
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RideRequestPersistenceException("Errore nella scrittura del file ride_requests.csv", e);
        }
    }
}
