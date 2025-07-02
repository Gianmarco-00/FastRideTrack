package org.ispw.fastridetrack.session;

import org.ispw.fastridetrack.adapter.EmailService;
import org.ispw.fastridetrack.adapter.GmailAdapter;
import org.ispw.fastridetrack.adapter.GoogleMapsAdapter;
import org.ispw.fastridetrack.adapter.MapService;
import org.ispw.fastridetrack.dao.*;
import org.ispw.fastridetrack.dao.mysql.SingletonDBSession;
import org.ispw.fastridetrack.model.Client;
import org.ispw.fastridetrack.model.Driver;

public class SessionManager {

    private static SessionManager instance;
    private final SessionFactory sessionFactory;
    private Client loggedClient;
    private Driver loggedDriver;

    private SessionManager() {
        // Carico la configurazione dalla variabile d'ambiente
        String usePersistenceEnv = System.getenv("USE_PERSISTENCE");

        // Determino la session factory in base al valore della variabile
        if ("true".equalsIgnoreCase(usePersistenceEnv)) {
            this.sessionFactory = new PersistenceSessionFactory();
        } else if ("file".equalsIgnoreCase(usePersistenceEnv)) {
            this.sessionFactory = new FileSystemSessionFactory();
        } else {
            this.sessionFactory = new InMemorySessionFactory();
        }

        // Adapter unificati per Gmail e GoogleMaps
        this.mapService = new GoogleMapsAdapter();
        this.emailService = new GmailAdapter();

    }


    public static void init() {
        if (instance == null) {
            instance = new SessionManager();
        }
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SessionManager non inizializzato. Chiama init() prima.");
        }
        return instance;
    }

    public ClientDAO getClientDAO() { return sessionFactory.createClientDAO(); }

    public DriverDAO getDriverDAO() { return sessionFactory.createDriverDAO(); }

    public RideRequestDAO getRideRequestDAO() {
        return sessionFactory.createRideRequestDAO();
    }

    public TaxiRideConfirmationDAO getTaxiRideDAO() {
        return sessionFactory.createTaxiRideDAO();
    }

    public RideDAO getRideDAO() {return sessionFactory.createRideDAO();}

    // === CLIENT ===
    public Client getLoggedClient() {
        return loggedClient;
    }

    public void setLoggedClient(Client client) {
        this.loggedClient = client;
    }

    // === DRIVER ===
    public Driver getLoggedDriver() {
        return loggedDriver;
    }

    public void setLoggedDriver(Driver driver) {
        this.loggedDriver = driver;
    }

    // === CLEAR SESSION ===
    public void clearSession() {
        System.out.println("Sessione utente terminata.");
        this.loggedClient = null;
        this.loggedDriver = null;
    }

    // === SERVIZI ESTERNI ===
    private final MapService mapService;
    private final EmailService emailService;

    public MapService getMapService() {
        return mapService;
    }

    public EmailService getEmailService() {
        return emailService;
    }

    // === CHIUSURA PULITA DEL DB ===
    public void shutdown() {
        if (sessionFactory instanceof PersistenceSessionFactory) {
            SingletonDBSession.reset();
            System.out.println("Connessione DB chiusa e singleton resettato.");
        }
    }

    public static void reset(){
        instance = null;
    }

}



