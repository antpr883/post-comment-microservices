package com.andev.cache.service;

import java.util.Map;
import java.util.Optional;

/**
 * Main API for user caching service.
 * Provides methods for external services to interact with user cache.
 */
public interface UserCacheService {

    /**
     * Get user by ID with caching strategy: Redis -> Database -> External API
     * @param userId user ID to lookup
     * @return Optional containing user data or empty if not found
     */
    Optional<Map<String, Object>> getUserById(Long userId);

    /**
     * Cache user data in Redis and Database
     * @param userData user data to cache
     */
    void cacheUser(Map<String, Object> userData);

    /**
     * Evict user from all caches (Redis and Database)
     * @param userId user ID to evict
     */
    void evictUser(Long userId);
}
