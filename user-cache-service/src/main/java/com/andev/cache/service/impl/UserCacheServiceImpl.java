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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of UserCacheService with optimized caching strategy.
 * Implements multi-tier caching: Redis -> Database -> External API
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
        log.debug("🔍 Starting cache lookup for userId: {}", userId);

        // Step 1: Try Redis cache first (fastest)
        Optional<Map<String, Object>> redisResult = redisUserCacheService.getUserById(userId);
        if (redisResult.isPresent()) {
            log.debug("✅ Found user {} in Redis cache", userId);
            return redisResult;
        }

        // Step 2: Try Database cache
        Optional<Map<String, Object>> dbResult = databaseUserCacheService.getUserById(userId);
        if (dbResult.isPresent()) {
            log.debug("✅ Found user {} in database cache, refreshing Redis", userId);
            // Refresh Redis from database
            redisUserCacheService.cacheUser(dbResult.get());
            return dbResult;
        }

        // Step 3: Try External API
        Optional<Map<String, Object>> apiResult = tryGetFromApi(userId);
        if (apiResult.isPresent()) {
            log.debug("✅ Found user {} in external API, caching in both Redis and Database", userId);
            // Cache in both Redis and Database
            cacheUser(apiResult.get());
            return apiResult;
        }

        log.warn("❌ User {} not found in any cache layer", userId);
        return Optional.empty();
    }

    @Override
    public Map<Long, Optional<Map<String, Object>>> getUsersByIds(List<Long> userIds) {
        log.debug("🔍 Starting bulk cache lookup for {} users", userIds.size());
        
        Map<Long, Optional<Map<String, Object>>> results = new ConcurrentHashMap<>();
        
        // Parallel processing for better performance
        userIds.parallelStream().forEach(userId -> {
            Optional<Map<String, Object>> userData = getUserById(userId);
            results.put(userId, userData);
        });
        
        long foundCount = results.values().stream().filter(Optional::isPresent).count();
        log.debug("✅ Completed bulk cache lookup, found {} out of {} users", foundCount, userIds.size());
        return results;
    }

    @Override
    public Optional<Map<String, Object>> getUserByUsername(String username) {
        log.debug("🔍 Looking up user by username: {}", username);
        
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
        log.debug("💾 Caching user {} in both Redis and Database", userId);
        
        try {
            redisUserCacheService.cacheUser(userData);
            databaseUserCacheService.cacheUser(userData);
            log.debug("✅ Successfully cached user {}", userId);
        } catch (Exception e) {
            log.error("❌ Failed to cache user {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    @Override
    public void cacheUsers(List<Map<String, Object>> usersData) {
        log.debug("💾 Starting bulk cache operation for {} users", usersData.size());
        
        try {
            // Parallel processing for better performance
            usersData.parallelStream().forEach(this::cacheUser);
            log.debug("✅ Successfully cached {} users", usersData.size());
        } catch (Exception e) {
            log.error("❌ Failed to bulk cache users: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void evictUser(Long userId) {
        log.debug("🗑️ Evicting user {} from both Redis and Database", userId);
        
        try {
            redisUserCacheService.evictUser(userId);
            databaseUserCacheService.evictUser(userId);
            log.debug("✅ Successfully evicted user {}", userId);
        } catch (Exception e) {
            log.error("❌ Failed to evict user {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    @Override
    public void evictUsers(List<Long> userIds) {
        log.debug("🗑️ Starting bulk evict operation for {} users", userIds.size());
        
        try {
            // Parallel processing for better performance
            userIds.parallelStream().forEach(this::evictUser);
            log.debug("✅ Successfully evicted {} users", userIds.size());
        } catch (Exception e) {
            log.error("❌ Failed to bulk evict users: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isUserCachedInRedis(Long userId) {
        return redisUserCacheService.isUserCached(userId);
    }

    @Override
    public boolean isUserCachedInDatabase(Long userId) {
        return databaseUserCacheService.isUserCached(userId);
    }

    @Override
    public Map<String, Object> getCacheStats() {
        log.debug("📊 Getting cache statistics");
        
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        try {
            // Redis stats
            Map<String, Object> redisStats = redisUserCacheService.getStats();
            stats.put("redis", redisStats);
            
            // Database stats
            stats.put("database.enabled", true);
            stats.put("database.connection", "active");
            
            // External API stats
            stats.put("external.api.enabled", true);
            
            log.debug("✅ Successfully retrieved cache statistics");
        } catch (Exception e) {
            log.error("❌ Failed to get cache statistics: {}", e.getMessage());
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    private Optional<Map<String, Object>> tryGetFromApi(Long userId) {
        try {
            log.debug("🌐 Fetching user summary from User Hub for userId: {}", userId);
            Optional<Map<String, Object>> result = userHubClient.getUserSummary(userId);
            
            if (result.isPresent()) {
                log.debug("✅ Successfully fetched user {} from external API", userId);
            } else {
                log.debug("⚠️ User {} not found in external API", userId);
            }
            
            return result;
        } catch (Exception e) {
            log.error("❌ Failed to fetch user {} from external API: {}", userId, e.getMessage());
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