package com.andev.cache.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for caching user data.
 * Implements complete caching flow: Redis -> PostgreSQL -> External API
 * Returns data as Map to avoid dependencies on specific DTOs.
 */
public interface UserCacheService {
    
    /**
     * Retrieves user data by ID with complete caching flow.
     * 1. Check Redis (hot cache)
     * 2. Check PostgreSQL (warm cache)
     * 3. Call external API (cold data)
     * 
     * @param userId user ID
     * @return Optional with Map of user data or empty if not found
     */
    Optional<Map<String, Object>> getUserById(Long userId);
    
    /**
     * Bulk operation to retrieve multiple users.
     * 
     * @param userIds list of user IDs
     * @return Map with userId -> Optional of user data
     */
    Map<Long, Optional<Map<String, Object>>> getUsersByIds(List<Long> userIds);
    
    /**
     * Retrieves user data by username.
     * 
     * @param username username
     * @return Optional with Map of user data or empty if not found
     */
    Optional<Map<String, Object>> getUserByUsername(String username);
    
    /**
     * Caches user data in Redis and PostgreSQL.
     * 
     * @param userData Map with user data
     */
    void cacheUser(Map<String, Object> userData);
    
    /**
     * Bulk operation to cache multiple users.
     * 
     * @param usersData list of Maps with user data
     */
    void cacheUsers(List<Map<String, Object>> usersData);
    
    /**
     * Removes user from Redis and PostgreSQL cache.
     * 
     * @param userId user ID to remove
     */
    void evictUser(Long userId);
    
    /**
     * Bulk operation to remove multiple users.
     * 
     * @param userIds list of user IDs to remove
     */
    void evictUsers(List<Long> userIds);
    
    /**
     * Checks if user is cached in Redis.
     * 
     * @param userId user ID
     * @return true if user is cached in Redis, false otherwise
     */
    boolean isUserCachedInRedis(Long userId);
    
    /**
     * Checks if user is cached in PostgreSQL.
     * 
     * @param userId user ID
     * @return true if user is cached in PostgreSQL, false otherwise
     */
    boolean isUserCachedInDatabase(Long userId);
    
    /**
     * Gets cache statistics.
     * 
     * @return Map with cache metrics
     */
    Map<String, Object> getCacheStats();
} 