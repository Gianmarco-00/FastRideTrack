package org.ispw.fastridetrack.exception;

public class TaxiRidePersistenceException extends RuntimeException {
    public TaxiRidePersistenceException(String message) {
        super(message);
    }

    public TaxiRidePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
