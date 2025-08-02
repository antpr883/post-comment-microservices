package com.andev.cache.service.impl;

import com.andev.cache.config.CacheServiceProperties;
import com.andev.cache.service.RedisUserCacheService;
import com.andev.cache.util.CacheUtils;
import com.andev.cache.util.UserCacheUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of RedisUserCacheService with bulk operations and pipeline support.
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
    public Map<Long, Optional<Map<String, Object>>> getUsersByIds(List<Long> userIds) {
        return CacheUtils.executeWithErrorHandling(
                () -> {
                    if (userIds.isEmpty()) {
                        return Collections.emptyMap();
                    }

                    log.debug("🔍 Bulk Redis get for {} users", userIds.size());
                    
                    // Use pipeline for better performance
                    List<String> keys = userIds.stream()
                            .map(this::getRedisKey)
                            .collect(Collectors.toList());

                    List<Object> values = redisTemplate.opsForValue().multiGet(keys);
                    
                    Map<Long, Optional<Map<String, Object>>> results = new HashMap<>();
                    for (int i = 0; i < userIds.size(); i++) {
                        Long userId = userIds.get(i);
                        Object value = values.get(i);
                        results.put(userId, parseCachedValue(value));
                    }
                    
                    long foundCount = results.values().stream().filter(Optional::isPresent).count();
                    log.debug("✅ Bulk Redis get: found {} out of {} users", foundCount, userIds.size());
                    return results;
                },
                "Failed to retrieve users from Redis cache",
                Collections.emptyMap()
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
                    log.debug("💾 Cached user {} in Redis with TTL {} minutes", userId, ttlMinutes);
                },
                "Failed to cache user in Redis"
        );
    }

    @Override
    public void cacheUsers(List<Map<String, Object>> usersData) {
        CacheUtils.executeWithErrorHandling(
                () -> {
                    if (usersData.isEmpty()) {
                        return;
                    }

                    log.debug("💾 Bulk Redis cache for {} users", usersData.size());
                    
                    // Use pipeline for better performance
                    redisTemplate.executePipelined(new SessionCallback<Object>() {
                        @Override
                        public Object execute(org.springframework.data.redis.core.RedisOperations operations) throws org.springframework.data.redis.RedisSystemException {
                            long ttlMinutes = properties.getRedisTtl().toMinutes();
                            
                            for (Map<String, Object> userData : usersData) {
                                Long userId = UserCacheUtils.extractUserId(userData);
                                String redisKey = getRedisKey(userId);
                                operations.opsForValue().set(redisKey, userData, ttlMinutes, TimeUnit.MINUTES);
                            }
                            return null;
                        }
                    });
                    
                    log.debug("✅ Bulk cached {} users in Redis", usersData.size());
                },
                "Failed to bulk cache users in Redis"
        );
    }

    @Override
    public void evictUser(Long userId) {
        CacheUtils.executeWithErrorHandling(
                () -> {
                    String redisKey = getRedisKey(userId);
                    redisTemplate.delete(redisKey);
                    log.debug("🗑️ Evicted user {} from Redis cache", userId);
                },
                "Failed to evict user from Redis"
        );
    }

    @Override
    public void evictUsers(List<Long> userIds) {
        CacheUtils.executeWithErrorHandling(
                () -> {
                    if (userIds.isEmpty()) {
                        return;
                    }

                    log.debug("🗑️ Bulk Redis evict for {} users", userIds.size());
                    
                    List<String> keys = userIds.stream()
                            .map(this::getRedisKey)
                            .collect(Collectors.toList());
                    
                    Long deletedCount = redisTemplate.delete(keys);
                    log.debug("✅ Bulk evicted {} users from Redis cache", deletedCount);
                },
                "Failed to bulk evict users from Redis"
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
                    Set<String> keys = redisTemplate.keys(pattern);
                    
                    if (keys != null && !keys.isEmpty()) {
                        Long deletedCount = redisTemplate.delete(keys);
                        log.debug("🧹 Cleaned up {} expired keys from Redis cache", deletedCount);
                    } else {
                        log.debug("🧹 No keys found for cleanup in Redis cache");
                    }
                },
                "Failed to cleanup expired data from Redis"
        );
    }

    @Override
    public Map<String, Object> getStats() {
        return CacheUtils.executeWithErrorHandling(
                () -> {
                    log.debug("📊 Getting Redis statistics");
                    
                    Map<String, Object> stats = new HashMap<>();
                    
                    // Get total keys
                    String pattern = properties.getRedisKeyPrefix() + "*";
                    Set<String> keys = redisTemplate.keys(pattern);
                    stats.put("totalKeys", keys != null ? keys.size() : 0);
                    
                    // Get memory usage
                    Properties info = redisTemplate.getConnectionFactory().getConnection().info();
                    stats.put("usedMemory", info.getProperty("used_memory"));
                    stats.put("usedMemoryPeak", info.getProperty("used_memory_peak"));
                    
                    // Get connection info
                    stats.put("connectedClients", info.getProperty("connected_clients"));
                    stats.put("totalCommandsProcessed", info.getProperty("total_commands_processed"));
                    
                    log.debug("✅ Successfully retrieved Redis statistics");
                    return stats;
                },
                "Failed to get Redis statistics",
                Collections.emptyMap()
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
            log.error("❌ Failed to parse cached string value: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String getRedisKey(Long userId) {
        return properties.getRedisKeyPrefix() + userId;
    }

    private byte[] serializeValue(Map<String, Object> userData) {
        try {
            return objectMapper.writeValueAsBytes(userData);
        } catch (Exception e) {
            log.error("❌ Failed to serialize user data: {}", e.getMessage());
            return new byte[0];
        }
    }
} 