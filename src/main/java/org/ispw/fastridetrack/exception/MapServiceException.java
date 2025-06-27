package org.ispw.fastridetrack.exception;


public class MapServiceException extends Exception {
    public MapServiceException(String message) {
        super(message);
    }

    public MapServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

