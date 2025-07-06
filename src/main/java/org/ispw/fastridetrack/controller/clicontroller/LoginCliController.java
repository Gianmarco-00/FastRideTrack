package org.ispw.fastridetrack.controller.clicontroller;

import jakarta.mail.MessagingException;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
import org.ispw.fastridetrack.exception.DriverDAOException;
import org.ispw.fastridetrack.exception.MapServiceException;
import org.ispw.fastridetrack.model.enumeration.UserType;
import org.ispw.fastridetrack.session.SessionManager;

import java.util.Scanner;

public class LoginCliController {

    private final Scanner scanner = new Scanner(System.in);
    private final ApplicationFacade facade;

    public LoginCliController() {
        facade = new ApplicationFacade();
    }

    public void start() throws MapServiceException, MessagingException, DriverDAOException {
        try {
            UserType loggedUserType = loginFlow();

            switch (loggedUserType) {
                case CLIENT -> new ClientCliController().startClientFlow();
                case DRIVER -> new DriverCliController().startDriverFlow();
                default -> System.out.println("Unrecognized user type.");
            }

        } finally {
            System.out.println("Closing application...");
            scanner.close();
            SessionManager.getInstance().shutdown();
        }
    }

    private UserType loginFlow() throws DriverDAOException {
        System.out.println("Welcome to FastRideTrack CLI!");

        while (true) {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            if (facade.login(username, password)) {
                UserType type = facade.getLoggedUserType();
                System.out.println("Logged in as " + type);
                return type;
            } else {
                System.out.println("Invalid credentials. Please try again.");
            }
        }
    }
}