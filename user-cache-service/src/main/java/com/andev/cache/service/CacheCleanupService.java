package com.andev.cache.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for cleaning up expired cache data from Redis and PostgreSQL.
 * Runs scheduled cleanup jobs to remove expired entries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheCleanupService {

    private final RedisUserCacheService redisUserCacheService;
    private final DatabaseUserCacheService databaseUserCacheService;

    /**
     * Scheduled cleanup job that runs daily at 2 AM.
     * Cleans up expired data from both Redis and PostgreSQL.
     */
    @Scheduled(cron = "${user-cache.cleanup.cron:0 0 2 * * ?}")
    public void cleanupExpiredData() {
        log.info("Starting scheduled cleanup of expired cache data");
        
        redisUserCacheService.cleanupExpiredData();
        databaseUserCacheService.cleanupExpiredData();
        
        log.info("Completed scheduled cleanup of expired cache data");
    }

    /**
     * Manual cleanup method that can be called on demand.
     */
    public void manualCleanup() {
        log.info("Starting manual cleanup of expired cache data");
        cleanupExpiredData();
    }
} 