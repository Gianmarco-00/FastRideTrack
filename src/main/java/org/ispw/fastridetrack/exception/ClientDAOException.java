package org.ispw.fastridetrack.exception;

public class ClientDAOException extends RuntimeException {
    public ClientDAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientDAOException(String message) {
        super(message);
    }
}

