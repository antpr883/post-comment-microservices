package com.andev.cache.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * Utility class for common cache operations and error handling.
 */
@Slf4j
public final class CacheUtils {

    private CacheUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Executes a cache operation with error handling.
     * 
     * @param operation operation to execute
     * @param errorMessage error message for logging
     * @param defaultValue default value to return on error
     * @return result of operation or default value
     */
    public static <T> T executeWithErrorHandling(
            Supplier<T> operation, 
            String errorMessage, 
            T defaultValue) {
        try {
            return operation.get();
        } catch (Exception e) {
            log.error("{}: {}", errorMessage, e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Executes a void cache operation with error handling.
     * 
     * @param operation operation to execute
     * @param errorMessage error message for logging
     */
    public static void executeWithErrorHandling(
            Runnable operation, 
            String errorMessage) {
        try {
            operation.run();
        } catch (Exception e) {
            log.error("{}: {}", errorMessage, e.getMessage());
        }
    }

    /**
     * Executes a cache operation with error handling and re-throws.
     * 
     * @param operation operation to execute
     * @param errorMessage error message for logging
     * @return result of operation
     * @throws RuntimeException if operation fails
     */
    public static <T> T executeWithErrorHandlingAndRethrow(
            Supplier<T> operation, 
            String errorMessage) {
        try {
            return operation.get();
        } catch (Exception e) {
            log.error("{}: {}", errorMessage, e.getMessage());
            throw new RuntimeException(errorMessage, e);
        }
    }
} 