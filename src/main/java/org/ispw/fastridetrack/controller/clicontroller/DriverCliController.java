package org.ispw.fastridetrack.controller.clicontroller;

import jakarta.mail.MessagingException;
import org.ispw.fastridetrack.bean.RideBean;
import org.ispw.fastridetrack.bean.TaxiRideConfirmationBean;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
import org.ispw.fastridetrack.exception.*;
import org.ispw.fastridetrack.model.Map;
import org.ispw.fastridetrack.model.enumeration.RideConfirmationStatus;
import org.ispw.fastridetrack.session.SessionManager;
import org.ispw.fastridetrack.util.DriverSessionContext;

import java.util.Scanner;

public class DriverCliController {

    private final Scanner scanner = new Scanner(System.in);

    private final ApplicationFacade facade;

    private static final String LOGOUT_OPTION = "[0] Logout and exit";

    private static final String OPTION = "Select an option: ";

    private static final String LOGOUT = "Logging out...";

    private static final String INVALID_OPTION = "Invalid option. Please try again.";

    public DriverCliController() {
        facade = new ApplicationFacade();
    }

    public void startDriverFlow() throws DriverDAOException, MapServiceException, MessagingException {
        boolean exit = false;

        while (!exit) {
            RideBean currentRide = DriverSessionContext.getInstance().getCurrentRide();

            System.out.println("\n--- DRIVER MENU ---");

            if (currentRide == null) {
                System.out.println("[1] View next PENDING ride confirmation");
                System.out.println("[2] Accept ride confirmation");
                System.out.println("[3] Reject ride confirmation");
                System.out.println(LOGOUT_OPTION);
                System.out.print(OPTION);

                String input = scanner.nextLine();

                switch (input) {
                    case "1": showNextPendingRideConfirmation(); break;
                    case "2": acceptPendingRideConfirmation(); break;
                    case "3": rejectPendingRideConfirmation(); break;
                    case "0":
                        System.out.println(LOGOUT);
                        exit = true;
                        break;
                    default:
                        System.out.println(INVALID_OPTION);
                }

            } else {
                System.out.printf("Active ride (ID %d) - Status: %s%n", currentRide.getRideID(), currentRide.getRideStatus());

                switch (currentRide.getRideStatus()) {
                    case INITIATED -> showInitiatedRideMenu();
                    case CLIENT_LOCATED -> showClientLocatedRideMenu();
                    case ONGOING -> showOngoingRideMenu();
                    case FINISHED -> {
                        System.out.println("Ride has finished. Logging out.");
                        exit = true;
                    }
                    default -> {
                        System.out.println("Unhandled ride status. Exiting.");
                        exit = true;
                    }
                }
            }
        }
    }

    private void showInitiatedRideMenu() {
        System.out.println("[1] View route: driver -> client");
        System.out.println("[2] Confirm client located");
        System.out.println(LOGOUT_OPTION);
        System.out.print(OPTION);

        String input = scanner.nextLine();

        switch (input) {
            case "1" -> showRideRoute("from driver to client location");
            case "2" -> confirmClientLocated();
            case "0" -> {
                System.out.println(LOGOUT);
                System.exit(0);
            }
            default -> System.out.println(INVALID_OPTION);
        }
    }

    private void showClientLocatedRideMenu() {
        System.out.println("[1] Start ride");
        System.out.println("[2] View ride route");
        System.out.println(LOGOUT_OPTION);
        System.out.print(OPTION);

        String input = scanner.nextLine();

        switch (input) {
            case "1" -> startRideIfClientNotFetched();
            case "2" -> showRideRoute("from driver to client location");
            case "0" -> {
                System.out.println(LOGOUT);
                System.exit(0);
            }
            default -> System.out.println(INVALID_OPTION);
        }
    }

    private void showOngoingRideMenu() {
        System.out.println("[1] View ride route");
        System.out.println(LOGOUT_OPTION);
        System.out.print(OPTION);

        String input = scanner.nextLine();

        switch (input) {
            case "1" -> showRideRoute("from client location to destination");
            case "0" -> {
                System.out.println(LOGOUT);
                System.exit(0);
            }
            default -> System.out.println(INVALID_OPTION);
        }
    }

    private void showNextPendingRideConfirmation() throws DriverDAOException, DriverUnavailableException {
        Integer driverID = SessionManager.getInstance().getLoggedDriver().getUserID();
        var optConf = facade.getNextRideConfirmation(driverID);

        if (optConf.isEmpty()) {
            System.out.println("No ride confirmations available.");
            return;
        }

        TaxiRideConfirmationBean conf = optConf.get();

        if (conf.getStatus() != RideConfirmationStatus.PENDING) {
            System.out.println("No PENDING confirmations.");
            return;
        }

        System.out.println("Next ride confirmation:");
        System.out.println("Ride ID: " + conf.getRideID());
        System.out.println("Client: " + conf.getClient().getName());
        System.out.println("Destination: " + conf.getDestination());
        System.out.printf("Estimated fare: €%.2f%n", conf.getEstimatedFare());
        System.out.printf("Estimated time: %.2f minutes%n", conf.getEstimatedTime());
        System.out.println("Confirmation status: " + conf.getStatus());
    }

    private void acceptPendingRideConfirmation() throws DriverDAOException, MapServiceException, MessagingException {
        Integer driverID = SessionManager.getInstance().getLoggedDriver().getUserID();

        try {
            var optConf = facade.getNextRideConfirmation(driverID);

            if (optConf.isEmpty()) {
                System.out.println("No confirmation to accept.");
                return;
            }

            TaxiRideConfirmationBean conf = optConf.get();

            if (conf.getStatus() != RideConfirmationStatus.PENDING) {
                System.out.println("No PENDING confirmations to accept.");
                return;
            }

            acceptAndInitializeRide(conf);

        } catch (DriverUnavailableException e) {
            System.out.println("Driver not available: " + e.getMessage());
        } catch (RideConfirmationNotFoundException e) {
            System.out.println("Ride confirmation not found: " + e.getMessage());
        } catch (RideConfirmationNotPendingException e) {
            System.out.println("Ride confirmation is no longer pending: " + e.getMessage());
        } catch (DriverMismatchException e) {
            System.out.println("Driver mismatch: " + e.getMessage());
        }
    }

    private void acceptAndInitializeRide(TaxiRideConfirmationBean conf) throws DriverDAOException, MapServiceException, MessagingException {
        try {
            facade.acceptRideConfirmationAndInitializeRide(conf);
            System.out.println("Ride confirmation accepted and ride initialized.");
        } catch (RideAlreadyActiveException e) {
            System.out.println("Ride already active.");
        }
    }

    private void rejectPendingRideConfirmation() throws DriverDAOException, MessagingException {
        Integer driverID = SessionManager.getInstance().getLoggedDriver().getUserID();

        try {
            var optConf = facade.getNextRideConfirmation(driverID);

            if (optConf.isEmpty()) {
                System.out.println("No confirmation to reject.");
                return;
            }

            TaxiRideConfirmationBean conf = optConf.get();

            if (conf.getStatus() != RideConfirmationStatus.PENDING) {
                System.out.println("No PENDING confirmations to reject.");
                return;
            }

            System.out.print("Enter rejection reason: ");
            String reason = scanner.nextLine();

            facade.rejectRideConfirmation(conf, reason);
            System.out.println("Ride confirmation rejected and client notified.");

        } catch (DriverUnavailableException e) {
            System.out.println("Driver not available: " + e.getMessage());
        } catch (RideConfirmationNotFoundException e) {
            System.out.println("Ride confirmation not found: " + e.getMessage());
        }
    }

    private void confirmClientLocated() {
        try {

            Map map = facade.loadDriverRouteBasedOnRideStatus();

            facade.markClientLocated(map.getEstimatedTimeMinutes());
            System.out.println("Client marked as located and notified.");

        } catch (Exception e) {
            System.out.println("Error marking client as located: " + e.getMessage());
        }
    }

    private void startRideIfClientNotFetched() {
        try {
            facade.startRide();
            System.out.println("Ride started successfully.");
        } catch (ClientNotFetchedException e) {
            System.out.println("Error:" + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error starting ride: " + e.getMessage());
        }
    }

    private void showRideRoute(String startEnd) {
        try {
            Map map = facade.loadDriverRouteBasedOnRideStatus();
            System.out.println("Ride route:" + startEnd);
            System.out.println("Starting point:" + facade.getAddressFromCoordinatesString(map.getOrigin()));
            System.out.println("End point:" + facade.getAddressFromCoordinatesString(map.getDestination()));
            System.out.println("Estimated distance: " + map.getDistanceKm() + " km");
            System.out.println("Estimated time: " + map.getEstimatedTimeMinutes() + " minutes");
        } catch (Exception e) {
            System.out.println("Error loading ride route: " + e.getMessage());
        }
    }
}

