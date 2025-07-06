package org.ispw.fastridetrack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.ispw.fastridetrack.dao.inmemory.RideDAOInMemory;
import org.ispw.fastridetrack.exception.RideNotFoundException;
import org.ispw.fastridetrack.model.Client;
import org.ispw.fastridetrack.model.Driver;
import org.ispw.fastridetrack.model.Ride;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.model.enumeration.RideStatus;

//Alexandru Gabriel Soare
class RideDAOTest {

    private RideDAOInMemory rideDAO;
    private Driver driver1;
    private Driver driver2;
    private Ride rideActive;
    private Ride rideFinished;

    @BeforeEach
    void setUp() {
        rideDAO = new RideDAOInMemory();

        driver1 = new Driver(1, "user1", "pass", "Driver One", "email1@example.com", "12345",
                0.0, 0.0, "car1", "plate1", "aff1", true);

        driver2 = new Driver(2, "user2", "pass", "Driver Two", "email2@example.com", "67890",
                0.0, 0.0, "car2", "plate2", "aff2", true);

        Client client1 = new Client(1, "client1", "pass", "Mario", "mario@email.com", "1234567890", PaymentMethod.CASH);

        rideActive = new Ride();
        rideActive.setRideID(100);
        rideActive.setDriver(driver1);
        rideActive.setClient(client1);
        rideActive.setStatus(RideStatus.ONGOING);

        rideFinished = new Ride();
        rideFinished.setRideID(101);
        rideFinished.setDriver(driver1);
        rideFinished.setClient(client1);
        rideFinished.setStatus(RideStatus.FINISHED);

        rideDAO.save(rideActive);
        rideDAO.save(rideFinished);
    }

    @Test
    void testFindActiveRideByDriver_returnsActiveRide() {
        Optional<Ride> activeRide = rideDAO.findActiveRideByDriver(driver1.getUserID());
        assertTrue(activeRide.isPresent());
        assertEquals(rideActive.getRideID(), activeRide.get().getRideID());
        assertNotEquals(RideStatus.FINISHED, activeRide.get().getStatus());
    }

    @Test
    void testFindActiveRideByDriver_noActiveRide() {
        Optional<Ride> activeRide = rideDAO.findActiveRideByDriver(driver2.getUserID());
        assertTrue(activeRide.isEmpty());
    }

    @Test
    void testSaveAssignsIdIfNull() {
        Ride newRide = new Ride();
        newRide.setRideID(null);
        newRide.setDriver(driver2);
        newRide.setStatus(RideStatus.INITIATED);

        rideDAO.save(newRide);

        assertNotNull(newRide.getRideID());
        assertTrue(rideDAO.exists(newRide.getRideID()));
    }

    @Test
    void testUpdateExistingRide() {
        rideActive.setStatus(RideStatus.FINISHED);
        rideDAO.update(rideActive);

        Optional<Ride> ride = rideDAO.findById(rideActive.getRideID());
        assertTrue(ride.isPresent());
        assertEquals(RideStatus.FINISHED, ride.get().getStatus());
    }

    @Test
    void testUpdateNonExistentRideThrows() {
        Ride fakeRide = new Ride();
        fakeRide.setRideID(999);
        fakeRide.setDriver(driver2);
        fakeRide.setStatus(RideStatus.INITIATED);

        assertThrows(RideNotFoundException.class, () -> rideDAO.update(fakeRide));
    }

    @Test
    void testExists() {
        assertTrue(rideDAO.exists(rideActive.getRideID()));
        assertFalse(rideDAO.exists(9999));
    }
}