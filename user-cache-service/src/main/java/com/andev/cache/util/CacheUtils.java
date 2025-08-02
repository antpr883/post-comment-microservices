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
            T result = operation.get();
            log.debug("✅ Cache operation completed successfully");
            return result;
        } catch (Exception e) {
            log.error("❌ Cache operation failed - {}: {}", errorMessage, e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("❌ Stack trace:", e);
            }
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
            log.debug("✅ Cache operation completed successfully");
        } catch (Exception e) {
            log.error("❌ Cache operation failed - {}: {}", errorMessage, e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("❌ Stack trace:", e);
            }
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
            T result = operation.get();
            log.debug("✅ Cache operation completed successfully");
            return result;
        } catch (Exception e) {
            log.error("❌ Cache operation failed - {}: {}", errorMessage, e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("❌ Stack trace:", e);
            }
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * Executes a cache operation with timing information.
     * 
     * @param operation operation to execute
     * @param operationName name of the operation for logging
     * @param defaultValue default value to return on error
     * @return result of operation or default value
     */
    public static <T> T executeWithTiming(
            Supplier<T> operation, 
            String operationName, 
            T defaultValue) {
        long startTime = System.currentTimeMillis();
        try {
            T result = operation.get();
            long duration = System.currentTimeMillis() - startTime;
            log.debug("⏱️ {} completed in {}ms", operationName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ {} failed after {}ms: {}", operationName, duration, e.getMessage());
            return defaultValue;
        }
    }
} 