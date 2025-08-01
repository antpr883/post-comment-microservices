package com.andev.cache.service.impl;

import com.andev.cache.config.UserCacheProperties;
import com.andev.cache.service.RedisUserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisUserCacheServiceImpl implements RedisUserCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserCacheProperties properties;

    @Override
    public Optional<Map<String, Object>> getUserById(Long userId) {
        try {
            String key = buildRedisKey(userId);
            Object value = redisTemplate.opsForValue().get(key);
            
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userData = (Map<String, Object>) value;
                log.debug("User found in Redis cache: {}", userId);
                return Optional.of(userData);
            }
            
            log.debug("User not found in Redis cache: {}", userId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error retrieving user from Redis cache: {}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    public void cacheUser(Long userId, Map<String, Object> userData) {
        try {
            String key = buildRedisKey(userId);
            redisTemplate.opsForValue().set(key, userData, properties.getCache().getRedisTtl());
            log.debug("User cached in Redis: {} with TTL: {}", userId, properties.getCache().getRedisTtl());
        } catch (Exception e) {
            log.error("Error caching user in Redis: {}", userId, e);
        }
    }

    @Override
    public void evictUser(Long userId) {
        try {
            String key = buildRedisKey(userId);
            redisTemplate.delete(key);
            log.debug("User evicted from Redis cache: {}", userId);
        } catch (Exception e) {
            log.error("Error evicting user from Redis cache: {}", userId, e);
        }
    }

    @Override
    public boolean exists(Long userId) {
        try {
            String key = buildRedisKey(userId);
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking user existence in Redis: {}", userId, e);
            return false;
        }
    }

    private String buildRedisKey(Long userId) {
        return properties.getCache().getRedisKeyPrefix() + userId;
    }
} 