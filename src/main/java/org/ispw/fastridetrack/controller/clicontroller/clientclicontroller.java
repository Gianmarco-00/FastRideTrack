package org.ispw.fastridetrack.controller.clicontroller;

import org.ispw.fastridetrack.bean.*;
import org.ispw.fastridetrack.controller.guicontroller.ApplicationFacade;
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


    public ClientCliController() throws Exception {
        facade = new ApplicationFacade();
    }


    public void start() {
        try {
            loginFlow();

            if (loggedUserType == UserType.CLIENT) {
                clientFlow();
            } else if (loggedUserType == UserType.DRIVER) {
                System.out.println("Accesso come DRIVER. Funzionalità CLI non ancora implementata.");
            } else {
                System.out.println("Tipo utente non riconosciuto.");
            }

        } catch (Exception e) {
            System.err.println("Errore: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Chiusura applicazione...");
            scanner.close();
            SessionManager.getInstance().shutdown();
        }
    }

    private void loginFlow() throws ClientDAOException, DriverDAOException {
        System.out.println("Benvenuto in FastRideTrack CLI!");
        boolean loggedIn = false;

        while (!loggedIn) {
            System.out.print("Inserisci username: ");
            String username = scanner.nextLine();

            System.out.print("Inserisci password: ");
            String password = scanner.nextLine();

            if (facade.login(username, password)) {
                loggedUserType = facade.getLoggedUserType();
                System.out.println("Login effettuato come " + loggedUserType);
                loggedIn = true;
            } else {
                System.out.println("Credenziali non valide, riprova.");
            }

        }
    }

    private void clientFlow() throws Exception {
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
            System.out.printf("[%d] %s - %s - %s - ETA: %s - Prezzo stimato: €%.2f\n",
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

