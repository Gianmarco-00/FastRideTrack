package org.ispw.fastridetrack;

import org.ispw.fastridetrack.bean.CoordinateBean;
import org.ispw.fastridetrack.bean.RideRequestBean;
import org.ispw.fastridetrack.controller.applicationcontroller.DriverMatchingApplicationController;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.session.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Gianmarco Manni
class SaveRideRequestTest {

    private DriverMatchingApplicationController controller;

    @BeforeEach
    void setUp() {
        System.setProperty("GMAIL_EMAIL", "fake@gmail.com");
        System.setProperty("GMAIL_APP_PASSWORD", "fakepassword");
        System.setProperty("USE_PERSISTENCE", "false");

        SessionManager.init();
        controller = new DriverMatchingApplicationController();
    }

    @Test
    void testSaveRideRequest() {
        CoordinateBean origin = new CoordinateBean(41.8902, 12.4924); // Roma
        RideRequestBean rideRequestBean = new RideRequestBean(origin, "Piazza Venezia", 5, PaymentMethod.CASH);

        RideRequestBean saved = controller.saveRideRequest(rideRequestBean);

        assertNotNull(saved, "La richiesta salvata non dovrebbe essere nulla");
        assertNotNull(saved.getRequestID(), "L'ID della richiesta deve essere non nullo");
        assertTrue(saved.getRequestID() > 0, "L'ID della richiesta deve essere maggiore di zero");
        assertEquals("Piazza Venezia", saved.getDestination());
        assertEquals(5, saved.getRadiusKm());
        assertEquals(PaymentMethod.CASH, saved.getPaymentMethod());

        CoordinateBean savedOrigin = saved.getOriginAsCoordinateBean();
        assertNotNull(savedOrigin);
        assertEquals(41.8902, savedOrigin.getLatitude(), 0.0001);
        assertEquals(12.4924, savedOrigin.getLongitude(), 0.0001);
    }

    @AfterEach
    void tearDown() {
        SessionManager.reset();
    }
}

