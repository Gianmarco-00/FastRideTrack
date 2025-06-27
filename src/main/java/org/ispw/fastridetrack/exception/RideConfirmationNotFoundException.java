package org.ispw.fastridetrack.exception;

public class RideConfirmationNotFoundException extends RuntimeException {
    public RideConfirmationNotFoundException(int rideID) {
        super("Conferma corsa con ID " + rideID + " non trovata");
    }
}

