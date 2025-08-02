package com.andev.comment.config.web;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.andev.comment.config.exception.NotFoundException;
import com.andev.comment.config.model.constants.api.ApiConstants;
import com.andev.comment.config.web.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for the comment service.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles NotFoundException and returns 404 Not Found.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommentNotFound(NotFoundException ex, WebRequest request) {

        logStackTrace(ex);

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles general RuntimeException and returns 500 Internal Server Error.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {

        logStackTrace(ex);

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void logStackTrace(Exception ex) {
        StringBuilder stackTrace = new StringBuilder();

        stackTrace.append(ApiConstants.ANSI_RED);

        stackTrace.append(ex.getMessage()).append(ApiConstants.BREAK_LINE);

        if (Objects.nonNull(ex.getCause())) {
            stackTrace.append(ex.getCause().getMessage()).append(ApiConstants.BREAK_LINE);
        }

        Arrays.stream(ex.getStackTrace())
                .filter(st -> st.getClassName().startsWith(ApiConstants.TIME_ZONE_PACKAGE_NAME))
                .forEach(st -> stackTrace
                        .append(st.getClassName())
                        .append(".")
                        .append(st.getMethodName())
                        .append(" (")
                        .append(st.getLineNumber())
                        .append(") "));

        log.error(stackTrace.append(ApiConstants.ANSI_WHITE).toString());
    }
}
