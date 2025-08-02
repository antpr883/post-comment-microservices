package com.andev.cache.util;

import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Optional;

/**
 * Result wrapper for cache operations.
 */
@Value
@Builder
public class CacheResult {
    
    boolean success;
    String source;
    Optional<Map<String, Object>> data;
    String errorMessage;
    
    /**
     * Creates a successful result with data.
     */
    public static CacheResult success(String source, Map<String, Object> data) {
        return CacheResult.builder()
                .success(true)
                .source(source)
                .data(Optional.of(data))
                .build();
    }
    
    /**
     * Creates a successful result without data.
     */
    public static CacheResult success(String source) {
        return CacheResult.builder()
                .success(true)
                .source(source)
                .data(Optional.empty())
                .build();
    }
    
    /**
     * Creates a failed result.
     */
    public static CacheResult failure(String source, String errorMessage) {
        return CacheResult.builder()
                .success(false)
                .source(source)
                .data(Optional.empty())
                .errorMessage(errorMessage)
                .build();
    }
    
    /**
     * Creates a not found result.
     */
    public static CacheResult notFound(String source) {
        return CacheResult.builder()
                .success(false)
                .source(source)
                .data(Optional.empty())
                .errorMessage("Not found")
                .build();
    }
} 