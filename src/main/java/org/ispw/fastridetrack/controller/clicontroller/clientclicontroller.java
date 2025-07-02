package org.ispw.fastridetrack.controller.clicontroller;

import jakarta.mail.MessagingException;
import org.ispw.fastridetrack.bean.*;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
import org.ispw.fastridetrack.exception.*;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.model.TemporaryMemory;
import org.ispw.fastridetrack.model.enumeration.UserType;
import org.ispw.fastridetrack.session.SessionManager;

import java.util.List;
import java.util.Scanner;

public class ClientCliController {

    private final Scanner scanner = new Scanner(System.in);

    private UserType loggedUserType = null;
    private final ApplicationFacade facade;


    public ClientCliController() throws ClientDAOException {
        facade = new ApplicationFacade();
    }

    public void start() {
        try {
            loginFlow();

            switch (loggedUserType) {
                case CLIENT -> clientFlow();
                case DRIVER -> System.out.println("Accesso come DRIVER. Funzionalità CLI non ancora implementata.");
                default -> System.out.println("Tipo utente non riconosciuto.");
            }

        } catch (LoginFailedCliException e) {
            System.err.println("Errore di login: " + e.getMessage());
        } catch (MapServiceException | DriverDAOException | MessagingException e) {
            throw new ClientCliRuntimeException("Errore fatale nella CLI Client", e);
        } finally {
            System.out.println("Chiusura applicazione...");
            scanner.close();
            SessionManager.getInstance().shutdown();
        }
    }


    private void loginFlow() throws LoginFailedCliException {
        System.out.println("Benvenuto in FastRideTrack CLI!");
        boolean loggedIn = false;
        int tentativi = 0;
        final int MAX_TENTATIVI = 3;

        while (!loggedIn && tentativi < MAX_TENTATIVI) {
            System.out.print("Inserisci username: ");
            String username = scanner.nextLine();

            System.out.print("Inserisci password: ");
            String password = scanner.nextLine();

            try {
                if (facade.login(username, password)) {
                    loggedUserType = facade.getLoggedUserType();
                    System.out.println("Login effettuato come " + loggedUserType);
                    loggedIn = true;
                } else {
                    tentativi++;
                    System.out.println("Credenziali non valide, riprova.");
                }
            } catch (ClientDAOException | DriverDAOException e) {
                throw new LoginFailedCliException("Errore durante il login: " + e.getMessage());
            }
        }

        if (!loggedIn) {
            throw new LoginFailedCliException("Numero massimo di tentativi di login superato.");
        }
    }


    private void clientFlow() throws ClientDAOException, MapServiceException, DriverDAOException, MessagingException {
        System.out.println("\n--- Inserisci la destinazione ---");
        System.out.print("Indirizzo di partenza: ");
        String originAddress = scanner.nextLine();

        CoordinateBean originCoords;
        try {
            originCoords = SessionManager.getInstance().getMapService().geocodeAddress(originAddress);
        } catch (MapServiceException e) {
            System.err.println("Errore nella geocodifica dell'indirizzo: " + e.getMessage());
            return;
        }

        System.out.print("Indirizzo destinazione: ");
        String destination = scanner.nextLine();

        System.out.print("Raggio di ricerca taxi (in km): ");
        int radiusKm;
        try {
            radiusKm = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Input non valido per il raggio. Uso valore di default 1 km.");
            radiusKm = 1;
        }

        // Popola TemporaryMemory tramite il facade
        MapRequestBean mapRequest = new MapRequestBean(originCoords, destination, radiusKm);
        TemporaryMemory.getInstance().setMapRequestBean(mapRequest);

        RideRequestBean requestBean = new RideRequestBean(originCoords, destination, radiusKm, null);
        requestBean.setClient(ClientBean.fromModel(SessionManager.getInstance().getLoggedClient()));


        // Salvataggio ride e recupero HTML mappa
        facade.processRideRequestAndReturnMapHtml(requestBean, mapRequest);

        // Calcolo mappa e driver
        facade.loadMapAndAvailableDriversForClient();

        List<AvailableDriverBean> availableDrivers = TemporaryMemory.getInstance().getAvailableDrivers();
        if (availableDrivers == null || availableDrivers.isEmpty()) {
            System.out.println("Nessun driver disponibile nel raggio selezionato.");
            return;
        }

        System.out.println("\nDriver disponibili:");
        for (int i = 0; i < availableDrivers.size(); i++) {
            AvailableDriverBean d = availableDrivers.get(i);
            System.out.printf("[%d] %s - %s - %s - ETA: %s - Prezzo stimato: €%.2f%n",
                    i + 1,
                    d.getName(),
                    d.getVehicleInfo(),
                    d.getVehiclePlate(),
                    d.getEstimatedTimeFormatted(),
                    d.getEstimatedPrice()
            );
        }

        int driverChoice = -1;
        while (driverChoice < 1 || driverChoice > availableDrivers.size()) {
            System.out.print("Seleziona il driver (numero): ");
            try {
                driverChoice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Input non valido.");
            }
        }

        AvailableDriverBean selectedDriver = availableDrivers.get(driverChoice - 1);

        System.out.println("\nMetodi di pagamento:");
        System.out.println("[1] Contanti");
        System.out.println("[2] Carta");

        int paymentChoice = -1;
        while (paymentChoice != 1 && paymentChoice != 2) {
            System.out.print("Seleziona metodo di pagamento (numero): ");
            try {
                paymentChoice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Input non valido.");
            }
        }

        PaymentMethod paymentMethod = (paymentChoice == 1) ? PaymentMethod.CASH : PaymentMethod.CARD;

        // Conferma richiesta e salva in memoria temporanea
        facade.confirmRideRequest(selectedDriver, paymentMethod);

        // Invia email e conferma corsa
        facade.confirmRideAndNotifyDriver();

        System.out.println("Corsa confermata. Email inviata al driver.");
    }


}

