package org.ispw.fastridetrack.exception;

public class RideConfirmationNotFoundException extends RuntimeException {
    public RideConfirmationNotFoundException(int rideID) {
        super("Confirmation ride with " + rideID + " not  found.");
    }
}

