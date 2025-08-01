package com.andev.cache.service;

import java.util.Map;
import java.util.Optional;

public interface DatabaseUserCacheService {

    /**
     * Get user from database cache
     */
    Optional<Map<String, Object>> getUserById(Long userId);

    /**
     * Cache user in database
     */
    void cacheUser(Long userId, Map<String, Object> userData);

    /**
     * Evict user from database cache
     */
    void evictUser(Long userId);

    /**
     * Check if user exists in database cache
     */
    boolean exists(Long userId);
} 