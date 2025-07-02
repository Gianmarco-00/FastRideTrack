package org.ispw.fastridetrack;

import org.ispw.fastridetrack.bean.AvailableDriverBean;
import org.ispw.fastridetrack.dao.inmemory.DriverDAOInMemory;
import org.ispw.fastridetrack.model.Coordinate;
import org.ispw.fastridetrack.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//Gianmarco Manni
class FindAvailableDriverTest {

    private DriverDAOInMemory driverDAO;

    @BeforeEach
    void setUp() {
        System.setProperty("GMAIL_EMAIL", "fake@gmail.com");
        System.setProperty("GMAIL_APP_PASSWORD", "fakepassword");
        System.setProperty("USE_PERSISTENCE", "false");

        SessionManager.init();
        driverDAO = new DriverDAOInMemory();
    }

    @Test
    void testDriversWithinRadius_Rome() {
        // Coordinate vicine a Marco Rossi e Giulia Verdi (zona Roma)
        Coordinate userLocation = new Coordinate(41.870, 12.500);
        int radiusKm = 5;

        List<AvailableDriverBean> availableDrivers = driverDAO.findDriversAvailableWithinRadius(userLocation, radiusKm);

        assertEquals(2, availableDrivers.size(), "Dovrebbero esserci 2 driver nel raggio di 5 km (Marco e Giulia)");

        boolean containsMarco = availableDrivers.stream()
                .anyMatch(d -> d.getUsername().equals("marco92"));

        boolean containsGiulia = availableDrivers.stream()
                .anyMatch(d -> d.getUsername().equals("giulia77"));

        assertTrue(containsMarco, "Marco dovrebbe essere incluso");
        assertTrue(containsGiulia, "Giulia dovrebbe essere inclusa");
    }

    @Test
    void testDriversWithinRadius_Novara() {
        // Coordinate vicine a Luca Bianchi e Giulia Rossi (zona Novara)
        Coordinate userLocation = new Coordinate(45.6385, 8.8340);
        int radiusKm = 3;

        List<AvailableDriverBean> availableDrivers = driverDAO.findDriversAvailableWithinRadius(userLocation, radiusKm);

        assertEquals(2, availableDrivers.size(), "Dovrebbero esserci 2 driver nel raggio di 3 km (Luca e Giulia)");

        boolean containsLuca = availableDrivers.stream()
                .anyMatch(d -> d.getUsername().equals("luca88"));

        boolean containsGiuliaR = availableDrivers.stream()
                .anyMatch(d -> d.getUsername().equals("giulia78"));

        assertTrue(containsLuca, "Luca dovrebbe essere incluso");
        assertTrue(containsGiuliaR, "Giulia Rossi dovrebbe essere inclusa");
    }

    @Test
    void testNoDriverWithinRadius() {
        // Coordinate lontane da tutti (per esempio Cagliari)
        Coordinate userLocation = new Coordinate(39.2238, 9.1217);
        int radiusKm = 5;

        List<AvailableDriverBean> availableDrivers = driverDAO.findDriversAvailableWithinRadius(userLocation, radiusKm);

        assertTrue(availableDrivers.isEmpty(), "Non dovrebbero esserci driver nel raggio");
    }

    @Test
    void testDriverIncludesEstimatedTimeAndPrice() {
        Coordinate userLocation = new Coordinate(41.870, 12.500); // zona Roma
        int radiusKm = 5;

        List<AvailableDriverBean> availableDrivers = driverDAO.findDriversAvailableWithinRadius(userLocation, radiusKm);

        assertFalse(availableDrivers.isEmpty(), "Ci si aspetta almeno un driver vicino");

        AvailableDriverBean marco = availableDrivers.stream()
                .filter(d -> d.getUsername().equals("marco92"))
                .findFirst()
                .orElse(null);

        assertNotNull(marco, "Marco deve essere presente tra i driver disponibili");

        // Controllo su valori stimati
        double estimatedTime = marco.getEstimatedTime();
        double estimatedPrice = marco.getEstimatedPrice();

        assertTrue(estimatedTime > 0, "Il tempo stimato deve essere maggiore di zero");
        assertTrue(estimatedPrice >= 3.0, "Il prezzo stimato deve essere almeno la tariffa base");

        // Verifica formattazione stringhe
        String timeFormatted = marco.getEstimatedTimeFormatted();
        String priceFormatted = marco.getEstimatedPriceFormatted();

        assertNotNull(timeFormatted);
        assertTrue(timeFormatted.matches("(\\d+h \\d{2}min|\\d+min)"), "Formato tempo stimato non valido");

        assertEquals("€" + String.format("%.2f", estimatedPrice), priceFormatted, "Formato prezzo stimato non corretto");
    }

}
