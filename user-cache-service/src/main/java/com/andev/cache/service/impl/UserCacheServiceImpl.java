package com.andev.cache.service.impl;


import com.andev.cache.client.UserHubClient;
import com.andev.cache.model.entities.User;
import com.andev.cache.repository.snapshot.UserSnapshotRepository;
import com.andev.cache.service.DatabaseUserCacheService;
import com.andev.cache.service.RedisUserCacheService;
import com.andev.cache.service.UserCacheService;
import com.andev.cache.util.CacheUtils;
import com.andev.cache.util.UserCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Implementation of UserCacheService.
 * Acts as an aggregator that orchestrates caching logic between Redis and Database.
 * Implements the multi-tier caching strategy: Redis -> Database -> External API
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

    @Override
    public Optional<Map<String, Object>> getUserById(Long userId) {
        log.debug("Starting cache lookup for userId: {}", userId);

        // Step 1: Try Redis cache first (fastest)
        Optional<Map<String, Object>> redisResult = redisUserCacheService.getUserById(userId);
        if (redisResult.isPresent()) {
            log.debug("Found user {} in Redis cache", userId);
            return redisResult;
        }

        // Step 2: Try Database cache
        Optional<Map<String, Object>> dbResult = databaseUserCacheService.getUserById(userId);
        if (dbResult.isPresent()) {
            log.debug("Found user {} in database cache, refreshing Redis", userId);
            // Refresh Redis from database
            redisUserCacheService.cacheUser(dbResult.get());
            return dbResult;
        }

        // Step 3: Try External API
        Optional<Map<String, Object>> apiResult = tryGetFromApi(userId);
        if (apiResult.isPresent()) {
            log.debug("Found user {} in external API, caching in both Redis and Database", userId);
            // Cache in both Redis and Database
            cacheUser(apiResult.get());
            return apiResult;
        }

        log.warn("User {} not found in any cache layer", userId);
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Object>> getUserByUsername(String username) {
        return CacheUtils.executeWithErrorHandling(
                () -> userSnapshotRepository.findByUsername(username)
                        .map(this::convertUserToMap),
                "Failed to retrieve user " + username + " from database cache",
                Optional.empty()
        );
    }

    @Override
    @Transactional
    public void cacheUser(Map<String, Object> userData) {
        Long userId = UserCacheUtils.extractUserId(userData);
        log.debug("Caching user {} in both Redis and Database", userId);
        
        redisUserCacheService.cacheUser(userData);
        databaseUserCacheService.cacheUser(userData);
    }

    @Override
    @Transactional
    public void evictUser(Long userId) {
        log.debug("Evicting user {} from both Redis and Database", userId);
        
        redisUserCacheService.evictUser(userId);
        databaseUserCacheService.evictUser(userId);
    }

    @Override
    public boolean isUserCachedInRedis(Long userId) {
        return redisUserCacheService.isUserCached(userId);
    }

    @Override
    public boolean isUserCachedInDatabase(Long userId) {
        return databaseUserCacheService.isUserCached(userId);
    }

    private Optional<Map<String, Object>> tryGetFromApi(Long userId) {
        try {
            log.debug("Fetching user summary from User Hub for userId: {}", userId);
            return userHubClient.getUserSummary(userId);
        } catch (Exception e) {
            log.error("Failed to fetch user {} from external API: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    private Map<String, Object> convertUserToMap(User user) {
        return Map.of(
            "userId", user.getUserId(),
            "username", user.getUsername(),
            "cachedAt", user.getCachedAt() != null ? user.getCachedAt().toString() : null,
            "expiresAt", user.getExpiresAt() != null ? user.getExpiresAt().toString() : null
        );
    }
} 