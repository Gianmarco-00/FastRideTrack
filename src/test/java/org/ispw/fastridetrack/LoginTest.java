package org.ispw.fastridetrack;

import org.ispw.fastridetrack.controller.applicationcontroller.LoginApplicationController;
import org.ispw.fastridetrack.model.enumeration.UserType;
import org.ispw.fastridetrack.session.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Gianmarco Manni
class LoginTest {

    private LoginApplicationController controller;

    @BeforeEach
    void setUp() {
        System.setProperty("GMAIL_EMAIL", "fake@gmail.com");
        System.setProperty("GMAIL_APP_PASSWORD", "fakepassword");
        System.setProperty("USE_PERSISTENCE", "false");

        SessionManager.init();
        controller = new LoginApplicationController();
    }

    @Test
    void testClientLoginSuccess() throws Exception {
        boolean result = controller.validateClientCredentials("testclient", "testpass", UserType.CLIENT);
        assertTrue(result);
    }

    @Test
    void testClientLoginWrongPassword() throws Exception {
        boolean result = controller.validateClientCredentials("testclient", "wrongpass", UserType.CLIENT);
        assertFalse(result);
    }

    @Test
    void testClientLoginNonExistingUser() throws Exception {
        boolean result = controller.validateClientCredentials("nouser", "nopass", UserType.CLIENT);
        assertFalse(result);
    }

    @AfterEach
    void tearDown() {
        SessionManager.reset();
    }
}

