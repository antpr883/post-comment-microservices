package com.andev.user.exception;

/**
 * Exception thrown for internal server errors
 */
public class InternalException extends RuntimeException {

    public InternalException(String message) {
        super(message);
    }

    public InternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
