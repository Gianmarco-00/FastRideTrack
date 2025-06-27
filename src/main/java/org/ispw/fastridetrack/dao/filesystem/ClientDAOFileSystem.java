package org.ispw.fastridetrack.dao.filesystem;

import org.ispw.fastridetrack.dao.ClientDAO;
import org.ispw.fastridetrack.model.Client;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClientDAOFileSystem implements ClientDAO {
    private static final String FILE_PATH = "src/data/clients.csv";

    public ClientDAOFileSystem() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                File parent = file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                boolean created = file.createNewFile();
                if (!created) {
                    System.err.println("Il file client CSV NON è stato creato perché esiste già o errore sconosciuto.");
                }
            } catch (IOException e) {
                System.err.println("Errore nella creazione del file client CSV: " + e.getMessage());
            }
        }

    }

    @Override
    public void save(Client client) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            String record = String.join(";",
                    String.valueOf(client.getUserID()),
                    client.getUsername(),
                    client.getPassword(),
                    client.getName(),
                    client.getEmail(),
                    client.getPhoneNumber(),
                    client.getPaymentMethod().name(),
                    String.valueOf(client.getLatitude()),
                    String.valueOf(client.getLongitude())
            );
            writer.write(record);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio del client: " + e.getMessage());
        }
    }

    @Override
    public Client findById(Integer idclient) {
        for (Client client : getAllClients()) {
            if (client.getUserID().equals(idclient)) {
                return client;
            }
        }
        return null;
    }

    @Override
    public Client retrieveClientByUsernameAndPassword(String username, String password) {
        for (Client client : getAllClients()) {
            if (client.getUsername().equals(username) && client.getPassword().equals(password)) {
                return client;
            }
        }
        return null;
    }

    private List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(FILE_PATH), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(";", -1);
                if (tokens.length < 9) {
                    continue;
                }

                Client client = parseClientFromLine(tokens);
                if (client != null) {
                    clients.add(client);
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nella lettura dei client da file: " + e.getMessage());
        }

        return clients;
    }

    private Client parseClientFromLine(String[] tokens) {
        try {
            Client client = new Client(
                    Integer.parseInt(tokens[0]),
                    tokens[1],
                    tokens[2],
                    tokens[3],
                    tokens[4],
                    tokens[5],
                    PaymentMethod.valueOf(tokens[6])
            );
            client.setLatitude(Double.parseDouble(tokens[7]));
            client.setLongitude(Double.parseDouble(tokens[8]));
            return client;
        } catch (Exception ignored) {
            return null; // righe malformate vengono ignorate
        }
    }
}




