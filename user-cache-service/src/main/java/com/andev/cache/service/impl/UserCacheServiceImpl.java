package com.andev.cache.service.impl;

import com.andev.cache.config.UserCacheProperties;
import com.andev.cache.model.domain.mapper.UserMapper;
import com.andev.cache.repository.UserSnapshotRepository;
import com.andev.cache.service.DatabaseUserCacheService;
import com.andev.cache.service.RedisUserCacheService;
import com.andev.cache.service.UserCacheService;
import com.andev.cache.service.UserHubClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Main implementation of user caching service.
 * Orchestrates caching strategy: Redis -> Database -> External API
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCacheServiceImpl implements UserCacheService {

    private final RedisUserCacheService redisUserCacheService;
    private final DatabaseUserCacheService databaseUserCacheService;
    private final UserHubClient userHubClient;
    private final UserSnapshotRepository userSnapshotRepository;
    private final UserMapper userMapper;
    private final UserCacheProperties properties;

    @Override
    public Optional<Map<String, Object>> getUserById(Long userId) {
        if (userId == null) {
            log.warn("User ID is null");
            return Optional.empty();
        }

        log.debug("Looking up user: {}", userId);

        // Step 1: Try Redis cache first
        Optional<Map<String, Object>> userData = redisUserCacheService.getUserById(userId);
        if (userData.isPresent()) {
            log.debug("User found in Redis cache: {}", userId);
            return userData;
        }

        // Step 2: Try Database cache
        userData = databaseUserCacheService.getUserById(userId);
        if (userData.isPresent()) {
            log.debug("User found in database cache: {}", userId);
            // Cache in Redis for next time
            redisUserCacheService.cacheUser(userId, userData.get());
            return userData;
        }

        // Step 3: Fetch from external service
        userData = userHubClient.fetchUserById(userId);
        if (userData.isPresent()) {
            log.debug("User fetched from external service: {}", userId);
            // Cache in both Redis and Database
            redisUserCacheService.cacheUser(userId, userData.get());
            databaseUserCacheService.cacheUser(userId, userData.get());
            return userData;
        }

        log.debug("User not found anywhere: {}", userId);
        return Optional.empty();
    }

    @Override
    public void cacheUser(Map<String, Object> userData) {
        if (userData == null) {
            log.warn("User data is null");
            return;
        }

        Long userId = (Long) userData.get("userId");
        if (userId == null) {
            log.warn("User ID is missing from user data");
            return;
        }

        log.debug("Caching user: {}", userId);
        
        // Cache in both Redis and Database
        redisUserCacheService.cacheUser(userId, userData);
        databaseUserCacheService.cacheUser(userId, userData);
    }

    @Override
    public void evictUser(Long userId) {
        if (userId == null) {
            log.warn("User ID is null");
            return;
        }

        log.debug("Evicting user from all caches: {}", userId);
        
        // Evict from both Redis and Database
        redisUserCacheService.evictUser(userId);
        databaseUserCacheService.evictUser(userId);
    }
}
