package com.andev.cache.service;

import java.util.Map;
import java.util.Optional;

public interface RedisUserCacheService {

    /**
     * Get user from Redis cache
     */
    Optional<Map<String, Object>> getUserById(Long userId);

    /**
     * Cache user in Redis
     */
    void cacheUser(Long userId, Map<String, Object> userData);

    /**
     * Evict user from Redis cache
     */
    void evictUser(Long userId);

    /**
     * Check if user exists in Redis cache
     */
    boolean exists(Long userId);
} 