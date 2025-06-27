package org.ispw.fastridetrack.exception;

public class DriverDAOException extends Exception {
    public DriverDAOException(String message) {
        super(message);
    }

    public DriverDAOException(String message, Throwable cause) {
        super(message, cause);
    }
}

