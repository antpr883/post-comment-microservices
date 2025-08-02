package com.andev.cache.service;

import java.util.List;
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
     * Bulk operation to retrieve multiple users from Redis cache.
     *
     * @param userIds list of user IDs to look up
     * @return Map containing user data for found users
     */
    Map<Long, Optional<Map<String, Object>>> getUsersByIds(List<Long> userIds);

    /**
     * Stores user data in Redis cache with TTL.
     *
     * @param userData the user data to cache
     */
    void cacheUser(Map<String, Object> userData);

    /**
     * Bulk operation to store multiple users in Redis cache.
     *
     * @param usersData list of user data to cache
     */
    void cacheUsers(List<Map<String, Object>> usersData);

    /**
     * Removes user data from Redis cache.
     *
     * @param userId the user ID to evict
     */
    void evictUser(Long userId);

    /**
     * Bulk operation to remove multiple users from Redis cache.
     *
     * @param userIds list of user IDs to evict
     */
    void evictUsers(List<Long> userIds);

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

    /**
     * Gets Redis cache statistics.
     *
     * @return Map containing Redis cache statistics
     */
    Map<String, Object> getStats();
} 