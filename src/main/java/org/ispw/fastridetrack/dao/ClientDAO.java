package org.ispw.fastridetrack.dao;

import org.ispw.fastridetrack.model.Client;

public interface ClientDAO {
    void save(Client client);
    Client findById(Integer idclient);
    Client retrieveClientByUsernameAndPassword(String username, String password);
}


