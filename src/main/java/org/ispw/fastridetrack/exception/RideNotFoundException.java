package org.ispw.fastridetrack.exception;

public class RideNotFoundException extends RuntimeException {
    public RideNotFoundException(int rideID) {
        super("Ride with ID " + rideID + "not found");
    }
}

