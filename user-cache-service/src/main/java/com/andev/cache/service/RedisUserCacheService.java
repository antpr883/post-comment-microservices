package com.andev.cache.service;

import java.util.Map;
import java.util.Optional;

/**
 * Service for managing user data in Redis cache.
 * Responsible only for Redis operations.
 */
public interface RedisUserCacheService {

    /**
     * Retrieves user data from Redis cache by user ID.
     *
     * @param userId the user ID to look up
     * @return Optional containing user data if found, empty otherwise
     */
    Optional<Map<String, Object>> getUserById(Long userId);

    /**
     * Stores user data in Redis cache with TTL.
     *
     * @param userData the user data to cache
     */
    void cacheUser(Map<String, Object> userData);

    /**
     * Removes user data from Redis cache.
     *
     * @param userId the user ID to evict
     */
    void evictUser(Long userId);

    /**
     * Checks if user is cached in Redis.
     *
     * @param userId the user ID to check
     * @return true if user is cached, false otherwise
     */
    boolean isUserCached(Long userId);

    /**
     * Cleans expired keys from Redis cache.
     */
    void cleanupExpiredData();
} 