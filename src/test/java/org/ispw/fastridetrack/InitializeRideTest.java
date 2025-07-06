package org.ispw.fastridetrack;

import org.ispw.fastridetrack.bean.*;
import org.ispw.fastridetrack.controller.applicationcontroller.CurrentRideManagementApplicationController;
import org.ispw.fastridetrack.exception.RideAlreadyActiveException;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.model.enumeration.RideStatus;
import org.ispw.fastridetrack.session.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

//Alexandru Gabriel Soare
class InitializeRideTest {

    private CurrentRideManagementApplicationController controller;
    private TaxiRideConfirmationBean confirmationBean;

    @BeforeEach
    void setUp() {
        System.setProperty("USE_PERSISTENCE", "false");
        System.setProperty("GMAIL_EMAIL", "fake@gmail.com");
        System.setProperty("GMAIL_APP_PASSWORD", "fakepassword");
        SessionManager.reset();
        SessionManager.init();

        controller = new CurrentRideManagementApplicationController();

        // Creo CoordinateBean (può essere null o valorizzato)
        CoordinateBean coordBean = new CoordinateBean(41.9, 12.5);

        // Creo ClientBean e DriverBean coerenti
        ClientBean clientBean = new ClientBean(
                "pass",
                "cli",
                1,
                "Mario",
                "mario@mail.com",
                "333",
                coordBean,
                PaymentMethod.CARD
        );

        DriverBean driverBean = new DriverBean(
                "pass",
                "drv",
                2,
                "Luigi",
                "luigi@mail.com",
                "333",
                coordBean,
                "Fiat",
                "AA111BB",
                "FastRide",
                true
        );

        confirmationBean = new TaxiRideConfirmationBean(
                null,
                driverBean,
                clientBean,
                coordBean,
                "Via Roma",
                null,  // status
                null,  // estimatedFare
                null,  // estimatedTime
                PaymentMethod.CARD,
                LocalDateTime.now()
        );
    }

    @Test
    void testInitializeCurrentRide_Success() throws RideAlreadyActiveException {
        RideBean rideBean = controller.initializeCurrentRide(confirmationBean);

        assertNotNull(rideBean);
        assertEquals(RideStatus.INITIATED, rideBean.getRideStatus());
        assertEquals(confirmationBean.getDestination(), rideBean.getDestination());
    }

    @Test
    void testInitializeCurrentRide_ThrowsRideAlreadyActiveException() throws RideAlreadyActiveException {
        // Prima chiama che va a buon fine
        controller.initializeCurrentRide(confirmationBean);

        // Chiama una seconda volta con lo stesso driver, dovrebbe lanciare eccezione
        RideAlreadyActiveException exception = assertThrows(
                RideAlreadyActiveException.class,
                () -> controller.initializeCurrentRide(confirmationBean)
        );

        assertEquals("Ride already active for driver: " + confirmationBean.getDriver().getUserID(), exception.getMessage());
    }

    @AfterEach
    void tearDown() {
        SessionManager.reset();
    }
}