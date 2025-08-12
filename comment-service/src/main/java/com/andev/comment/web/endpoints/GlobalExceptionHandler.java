package com.andev.comment.web.endpoints;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.andev.comment.exception.NotFoundException;
import com.andev.comment.model.constants.ApiConstants;
import com.andev.comment.web.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for Comment Service.
 *
 * Handles all exceptions thrown by controllers and provides consistent error responses.
 * Includes proper logging, stack trace filtering, and specific error handling for
 * different exception types like validation errors, data access issues, and business logic exceptions.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${app.logging.include-stack-trace:false}")
    private boolean includeStackTrace;

    @Value("${app.logging.max-stack-trace-elements:10}")
    private int maxStackTraceElements;

    /**
     * Handles NotFoundException and returns 404 Not Found.
     * Used when requested comments or related entities are not found.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, WebRequest request) {
        logException(ex, "Resource not found");

        ErrorResponse errorResponse =
                createErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Overrides parent method to handle validation errors with custom response format.
     * Returns detailed validation error messages.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            org.springframework.http.HttpHeaders headers,
            org.springframework.http.HttpStatusCode status,
            WebRequest request) {

        logException(ex, "Validation failed");

        String validationMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((s1, s2) -> s1 + "; " + s2)
                .orElse("Validation failed");

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST, "Validation Error", validationMessage, request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles database access exceptions.
     * Returns generic error message to avoid exposing database details.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex, WebRequest request) {
        logException(ex, "Database access error");

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Database Error",
                "A database error occurred. Please try again later.",
                request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles general IllegalArgumentException (business logic violations).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        logException(ex, "Invalid argument");

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST, "Invalid Request", ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other RuntimeExceptions as internal server errors.
     * This is the fallback handler for unexpected errors.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        logException(ex, "Unexpected runtime error");

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Helper method to create standardized error responses.
     * Centralizes error response creation with consistent structure.
     */
    private ErrorResponse createErrorResponse(HttpStatus status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), error, message, path);
    }

    /**
     * Logs exceptions with proper stack trace filtering.
     * Only includes relevant application stack trace elements to avoid noise.
     */
    private void logException(Exception ex, String context) {
        log.error("{}: {}", context, ex.getMessage());

        if (includeStackTrace) {
            StringBuilder stackTrace = new StringBuilder();
            stackTrace.append(ApiConstants.ANSI_RED);
            stackTrace
                    .append("Exception: ")
                    .append(ex.getClass().getSimpleName())
                    .append(" - ")
                    .append(ex.getMessage())
                    .append(ApiConstants.BREAK_LINE);

            if (Objects.nonNull(ex.getCause())) {
                stackTrace
                        .append("Caused by: ")
                        .append(ex.getCause().getMessage())
                        .append(ApiConstants.BREAK_LINE);
            }

            // Filter stack trace to show only relevant application classes
            Arrays.stream(ex.getStackTrace())
                    .filter(st -> st.getClassName().startsWith("com.andev.comment"))
                    .limit(maxStackTraceElements)
                    .forEach(st -> stackTrace
                            .append("  at ")
                            .append(st.getClassName())
                            .append(".")
                            .append(st.getMethodName())
                            .append("(")
                            .append(st.getFileName())
                            .append(":")
                            .append(st.getLineNumber())
                            .append(")")
                            .append(ApiConstants.BREAK_LINE));

            log.error(stackTrace.append(ApiConstants.ANSI_WHITE).toString());
        }
    }
}
