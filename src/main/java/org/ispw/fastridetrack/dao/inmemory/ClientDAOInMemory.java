package org.ispw.fastridetrack.dao.inmemory;

import org.ispw.fastridetrack.dao.ClientDAO;
import org.ispw.fastridetrack.model.Client;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ClientDAOInMemory implements ClientDAO {

    private final Map<String, Client> clients = new ConcurrentHashMap<>();

    // Costruttore con client di test pre-caricato
    public ClientDAOInMemory() {
        Client testClient = new Client(
                1,
                "testclient",
                "testpass",
                "Mario Rossi",
                "mario@gmail.com",
                "1234567890",
                PaymentMethod.CARD
        );
        clients.put(testClient.getUsername(), testClient);
    }

    @Override
    public void save(Client client) {
        clients.put(client.getUsername(), client);
    }

    @Override
    public Client findById(Integer idclient) {
        // Map indexed by username → bisogna scorrere tutti
        for (Client c : clients.values()) {
            if (c.getUserID().equals(idclient)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Client retrieveClientByUsernameAndPassword(String username, String password) {
        Client client = clients.get(username);
        if (client != null && client.getPassword().equals(password)) {
            return client;
        }
        return null;
    }
}



