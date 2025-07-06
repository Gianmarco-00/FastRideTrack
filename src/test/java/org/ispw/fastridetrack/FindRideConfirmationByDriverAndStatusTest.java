package org.ispw.fastridetrack;

import org.ispw.fastridetrack.dao.inmemory.TaxiRideConfirmationDAOInMemory;
import org.ispw.fastridetrack.model.Client;
import org.ispw.fastridetrack.model.Coordinate;
import org.ispw.fastridetrack.model.Driver;
import org.ispw.fastridetrack.model.TaxiRideConfirmation;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.model.enumeration.RideConfirmationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//Alexandru Gabriel Soare
class FindRideConfirmationByDriverAndStatusTest {

    private TaxiRideConfirmationDAOInMemory dao;

    private Driver commonDriver;
    private Client client;

    @BeforeEach
    void setup() {
        dao = new TaxiRideConfirmationDAOInMemory();

        commonDriver = new Driver(100, "driver100", "pw", "Nome", "mail", "333", 45.0, 8.0, "Fiat", "AA123BB", "AFF", true);
        client = new Client(10, "client", "pw", "Mario", "mail", "333", PaymentMethod.CARD);

        TaxiRideConfirmation c1 = new TaxiRideConfirmation(
                301, commonDriver, client,
                new Coordinate(45, 8), "Dest1",
                RideConfirmationStatus.PENDING,
                15.0, 10.0, PaymentMethod.CARD,
                LocalDateTime.of(2025, 7, 2, 12, 30)
        );

        TaxiRideConfirmation c2 = new TaxiRideConfirmation(
                302, commonDriver, client,
                new Coordinate(45, 8), "Dest2",
                RideConfirmationStatus.PENDING,
                16.0, 9.0, PaymentMethod.CARD,
                LocalDateTime.of(2025, 7, 2, 12, 15) // prima
        );

        TaxiRideConfirmation c3 = new TaxiRideConfirmation(
                303, commonDriver, client,
                new Coordinate(45, 8), "Dest3",
                RideConfirmationStatus.ACCEPTED, // status diverso
                18.0, 8.0, PaymentMethod.CASH,
                LocalDateTime.of(2025, 7, 2, 12, 45)
        );

        TaxiRideConfirmation c4 = new TaxiRideConfirmation(
                304, commonDriver, client,
                new Coordinate(45, 8), "Dest4",
                RideConfirmationStatus.PENDING,
                20.0, 7.0, PaymentMethod.CARD,
                null // null time, deve finire in fondo
        );

        dao.save(c1);
        dao.save(c2);
        dao.save(c3);
        dao.save(c4);
    }

    @Test
    void testFindByDriverIDandStatus_ReturnsSortedList() {
        List<TaxiRideConfirmation> result = dao.findByDriverIDandStatus(100, RideConfirmationStatus.PENDING);
        assertEquals(3, result.size());

        // ordinamento: c2 (12:15), c1 (12:30), c4 (null)
        assertEquals(302, result.get(0).getRideID());
        assertEquals(301, result.get(1).getRideID());
        assertEquals(304, result.get(2).getRideID());
    }

    @Test
    void testFindByDriverIDandStatus_NoMatchForStatus() {
        List<TaxiRideConfirmation> result = dao.findByDriverIDandStatus(100, RideConfirmationStatus.REJECTED);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByDriverIDandStatus_DriverNotFound() {
        List<TaxiRideConfirmation> result = dao.findByDriverIDandStatus(999, RideConfirmationStatus.PENDING);
        assertTrue(result.isEmpty());
    }
}