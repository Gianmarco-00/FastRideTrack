package org.ispw.fastridetrack.dao.mysql;

import org.ispw.fastridetrack.exception.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SingletonDBSession {
    private Connection connection;

    private SingletonDBSession() throws DatabaseConnectionException {
        try {
            String url = System.getenv("DB_URL");
            String username = System.getenv("DB_USERNAME");
            String password = System.getenv("DB_PASSWORD");

            if (url == null || username == null || password == null) {
                throw new DatabaseConnectionException("Le variabili d'ambiente per il database non sono state configurate correttamente.");
            }

            System.out.println("[DEBUG] Connessione a MySQL...");
            System.out.println("URL: " + url);
            System.out.println("Utente: " + username);

            connection = DriverManager.getConnection(url, username, password);
            System.out.println("[DEBUG] Connessione stabilita.");
        } catch (SQLException e) {
            System.err.println("[ERRORE] Connessione MySQL fallita:");
            e.printStackTrace();
            throw new DatabaseConnectionException("Connessione al database non riuscita", e);
        }
    }

    // Inner static class per la lazy initialization thread-safe
    private static class Holder {
        private static final SingletonDBSession INSTANCE;

        static {
            try {
                INSTANCE = new SingletonDBSession();
            } catch (DatabaseConnectionException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    public static SingletonDBSession getInstance() {
        String usePersistenceEnv = System.getenv("USE_PERSISTENCE");
        if (!"true".equalsIgnoreCase(usePersistenceEnv)) {
            throw new IllegalStateException("Tentativo di inizializzare il DB in modalità non persistente");
        }
        return Holder.INSTANCE;
    }


    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DEBUG] Connessione MySQL chiusa.");
            } catch (SQLException e) {
                System.err.println("[WARN] Errore durante la chiusura della connessione:");
                e.printStackTrace();
            }
        }
    }

    public static synchronized void reset() {
        if (Holder.INSTANCE != null) {
            Holder.INSTANCE.closeConnection();
        }
    }
}






