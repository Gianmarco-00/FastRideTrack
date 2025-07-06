package org.ispw.fastridetrack.exception;

public class DriverUnavailableException extends RuntimeException {
    public DriverUnavailableException(String message) {
        super(message);
    }

    public DriverUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
