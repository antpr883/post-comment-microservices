package com.andev.user.web.endpoints;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.andev.user.exception.InternalException;
import com.andev.user.exception.NotFoundException;
import com.andev.user.model.constants.ApiConstants;
import com.andev.user.web.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for User Service.
 *
 * Provides centralized error handling for all user-related operations including
 * validation errors, database issues, business logic exceptions, and authentication failures.
 * Returns standardized error responses and proper HTTP status codes.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${app.logging.include-stack-trace:false}")
    private boolean includeStackTrace;

    @Value("${app.logging.max-stack-trace-elements:10}")
    private int maxStackTraceElements;

    /**
     * Handles NotFoundException (user not found, role not found).
     * Returns 404 with specific error message.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, WebRequest request) {
        logException(ex, "Resource not found");

        ErrorResponse errorResponse =
                createErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles custom business logic exceptions.
     * Returns 500 with custom error message from business layer.
     */
    @ExceptionHandler(InternalException.class)
    public ResponseEntity<ErrorResponse> handleInternalException(InternalException ex, WebRequest request) {
        logException(ex, "Business logic error");

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Business Logic Error",
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Overrides parent method to handle validation errors with custom response format.
     * Returns detailed field-level validation errors.
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
     * Returns generic error to avoid exposing database internals.
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
     * Handles illegal arguments (invalid input parameters).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        logException(ex, "Invalid argument");

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST, "Invalid Request", ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Fallback handler for unexpected runtime exceptions.
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
     * Ensures consistent error response structure across all endpoints.
     */
    private ErrorResponse createErrorResponse(HttpStatus status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), error, message, path);
    }

    /**
     * Logs exceptions with proper filtering and formatting.
     * Only includes application-specific stack trace elements for clarity.
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

            // Filter stack trace to show only relevant user service classes
            Arrays.stream(ex.getStackTrace())
                    .filter(st -> st.getClassName().startsWith("com.andev.user"))
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
