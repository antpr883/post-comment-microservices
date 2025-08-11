package com.andev.user.exception;

/**
 * Exception thrown when data already exists
 */
public class DataExistException extends RuntimeException {

    public DataExistException(String message) {
        super(message);
    }

    public DataExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
