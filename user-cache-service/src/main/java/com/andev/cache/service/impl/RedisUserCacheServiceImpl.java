package com.andev.cache.service.impl;

import com.andev.cache.config.CacheServiceProperties;
import com.andev.cache.service.RedisUserCacheService;
import com.andev.cache.util.CacheUtils;
import com.andev.cache.util.UserCacheUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of RedisUserCacheService.
 * Handles all Redis-specific operations for user caching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisUserCacheServiceImpl implements RedisUserCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheServiceProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<Map<String, Object>> getUserById(Long userId) {
        return CacheUtils.executeWithErrorHandling(
                () -> {
                    String redisKey = getRedisKey(userId);
                    Object cachedValue = redisTemplate.opsForValue().get(redisKey);
                    return parseCachedValue(cachedValue);
                },
                "Failed to retrieve user " + userId + " from Redis cache",
                Optional.empty()
        );
    }

    @Override
    public void cacheUser(Map<String, Object> userData) {
        CacheUtils.executeWithErrorHandling(
                () -> {
                    Long userId = UserCacheUtils.extractUserId(userData);
                    String redisKey = getRedisKey(userId);
                    long ttlMinutes = properties.getRedisTtl().toMinutes();
                    
                    redisTemplate.opsForValue().set(redisKey, userData, ttlMinutes, TimeUnit.MINUTES);
                    log.debug("Cached user {} in Redis with TTL {} minutes", userId, ttlMinutes);
                },
                "Failed to cache user in Redis"
        );
    }

    @Override
    public void evictUser(Long userId) {
        CacheUtils.executeWithErrorHandling(
                () -> {
                    String redisKey = getRedisKey(userId);
                    redisTemplate.delete(redisKey);
                    log.debug("Evicted user {} from Redis cache", userId);
                },
                "Failed to evict user from Redis"
        );
    }

    @Override
    public boolean isUserCached(Long userId) {
        return CacheUtils.executeWithErrorHandling(
                () -> {
                    String redisKey = getRedisKey(userId);
                    return redisTemplate.hasKey(redisKey);
                },
                "Failed to check if user " + userId + " is cached in Redis",
                false
        );
    }

    @Override
    public void cleanupExpiredData() {
        CacheUtils.executeWithErrorHandling(
                () -> {
                    String pattern = properties.getRedisKeyPrefix() + "*";
                    redisTemplate.delete(redisTemplate.keys(pattern));
                    log.debug("Cleaned up expired data from Redis cache");
                },
                "Failed to cleanup expired data from Redis"
        );
    }

    private Optional<Map<String, Object>> parseCachedValue(Object cachedValue) {
        if (cachedValue == null) {
            return Optional.empty();
        }
        
        if (cachedValue instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> userData = (Map<String, Object>) cachedValue;
            return Optional.of(userData);
        }
        
        if (cachedValue instanceof String) {
            return parseStringValue((String) cachedValue);
        }
        
        return Optional.empty();
    }

    private Optional<Map<String, Object>> parseStringValue(String cachedValue) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> userData = objectMapper.readValue(cachedValue, Map.class);
            return Optional.of(userData);
        } catch (Exception e) {
            log.error("Failed to parse cached string value: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String getRedisKey(Long userId) {
        return properties.getRedisKeyPrefix() + userId;
    }
} 