package com.andev.cache.service.impl;


import com.andev.cache.config.CacheServiceProperties;
import com.andev.cache.model.entities.User;
import com.andev.cache.repository.snapshot.UserSnapshotRepository;
import com.andev.cache.service.DatabaseUserCacheService;
import com.andev.cache.util.CacheUtils;
import com.andev.cache.util.UserCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of DatabaseUserCacheService.
 * Handles all PostgreSQL-specific operations for user caching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DatabaseUserCacheServiceImpl implements DatabaseUserCacheService {

    private final UserSnapshotRepository userSnapshotRepository;
    private final CacheServiceProperties properties;

    @Override
    public Optional<Map<String, Object>> getUserById(Long userId) {
        log.debug("🔍 Looking for user with ID: {} in database", userId);
        
        return CacheUtils.executeWithErrorHandling(
                () -> {
                    log.debug("🔍 Executing findById({}) query", userId);
                    Optional<User> userOptional = userSnapshotRepository.findById(userId);
                    
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        log.debug("✅ Found user in database - ID: {}, Username: {}, CachedAt: {}, ExpiresAt: {}", 
                                user.getUserId(), user.getUsername(), user.getCachedAt(), user.getExpiresAt());
                        return Optional.of(convertUserToMap(user));
                    } else {
                        log.debug("❌ User with ID {} not found in database", userId);
                        return Optional.empty();
                    }
                },
                "Failed to retrieve user " + userId + " from database cache",
                Optional.empty()
        );
    }

    @Override
    @Transactional
    public void cacheUser(Map<String, Object> userData) {
        CacheUtils.executeWithErrorHandlingAndRethrow(
                () -> {
                    User user = createUserFromData(userData);
                    userSnapshotRepository.save(user);
                    log.debug("💾 Cached user {} in database with TTL {} hours", 
                            user.getUserId(), properties.getDatabaseTtl().toHours());
                    return null;
                },
                "Failed to cache user in database"
        );
    }

    @Override
    @Transactional
    public void evictUser(Long userId) {
        CacheUtils.executeWithErrorHandling(
                () -> {
                    userSnapshotRepository.deleteById(userId);
                    log.debug("🗑️ Evicted user {} from database cache", userId);
                },
                "Failed to evict user from database"
        );
    }

    @Override
    public boolean isUserCached(Long userId) {
        return CacheUtils.executeWithErrorHandling(
                () -> userSnapshotRepository.existsById(userId),
                "Failed to check if user " + userId + " is cached in database",
                false
        );
    }

    @Override
    @Transactional
    public void cleanupExpiredData() {
        CacheUtils.executeWithErrorHandling(
                () -> {
                    LocalDateTime expirationTime = LocalDateTime.now();
                    userSnapshotRepository.deleteExpiredUsers(expirationTime);
                    log.debug("🧹 Cleaned up expired data from database cache");
                },
                "Failed to cleanup expired data from database"
        );
    }

    private User createUserFromData(Map<String, Object> userData) {
        User user = new User();
        user.setUserId(UserCacheUtils.extractUserId(userData));
        user.setUsername(UserCacheUtils.extractUsername(userData));
        user.setCachedAt(LocalDateTime.now());
        user.setExpiresAt(LocalDateTime.now().plus(properties.getDatabaseTtl()));
        return user;
    }

    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUserId());
        userData.put("username", user.getUsername());
        userData.put("cachedAt", user.getCachedAt() != null ? user.getCachedAt().toString() : null);
        userData.put("expiresAt", user.getExpiresAt() != null ? user.getExpiresAt().toString() : null);
        return userData;
    }
} 