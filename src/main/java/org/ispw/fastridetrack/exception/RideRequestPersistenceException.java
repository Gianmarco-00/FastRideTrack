package org.ispw.fastridetrack.exception;

public class RideRequestPersistenceException extends RuntimeException {
    public RideRequestPersistenceException(String message) {
        super(message);
    }

    public RideRequestPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
