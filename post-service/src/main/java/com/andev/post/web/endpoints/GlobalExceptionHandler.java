package com.andev.post.web.endpoints;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.andev.post.exception.NotFoundException;
import com.andev.post.model.constants.ApiConstants;
import com.andev.post.web.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for Post Service.
 *
 * Handles all post-related exceptions including RSQL parsing errors,
 * validation issues, database problems, and provides standardized error responses.
 * Special handling for Spring Data sorting exceptions and RSQL query validation.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${app.logging.include-stack-trace:false}")
    private boolean includeStackTrace;

    @Value("${app.logging.max-stack-trace-elements:10}")
    private int maxStackTraceElements;

    /**
     * Handles NotFoundException for posts not found.
     * Returns 404 with specific error message.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, WebRequest request) {
        logException(ex, "Post not found");

        ErrorResponse errorResponse =
                createErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles PropertyReferenceException for invalid sorting fields.
     * Returns detailed error message with valid field names for RSQL queries.
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReferenceException(
            PropertyReferenceException ex, WebRequest request) {
        logException(ex, "Invalid sort field");

        String validFields = "id, title, content, authorId, postStatus, likes, created, updated";
        String message =
                String.format("Invalid sorting field: '%s'. Valid fields are: %s", ex.getPropertyName(), validFields);

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST, "Invalid Sort Field", message, request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Overrides parent method to handle validation errors with custom response format.
     * Returns detailed field-level validation messages.
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
     * Returns generic error to avoid exposing database details.
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
     * Handles IllegalArgumentException for invalid RSQL queries and parameters.
     * Returns specific error message for RSQL parsing issues.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        logException(ex, "Invalid argument or RSQL query");

        String message = ex.getMessage();
        if (message != null && message.toLowerCase().contains("rsql")) {
            message = "Invalid RSQL query: " + message;
        }

        ErrorResponse errorResponse =
                createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Request", message, request.getDescription(false));

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

            // Filter stack trace to show only relevant post service classes
            Arrays.stream(ex.getStackTrace())
                    .filter(st -> st.getClassName().startsWith("com.andev.post"))
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
