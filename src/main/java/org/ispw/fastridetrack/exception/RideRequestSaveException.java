package org.ispw.fastridetrack.exception;

public class RideRequestSaveException extends RuntimeException {
    public RideRequestSaveException(String message) {
        super(message);
    }

    public RideRequestSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}

