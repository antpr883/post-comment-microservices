package com.andev.cache.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration properties for User Cache Service.
 * Supports both PostgreSQL snapshot database and Redis caching.
 */
@Data
@Component
public class CacheServiceProperties {
    
    // Database Configuration
    @Value("${spring.snapshot-datasource.url}")
    private String databaseUrl;
    
    @Value("${spring.snapshot-datasource.username}")
    private String databaseUsername;
    
    @Value("${spring.snapshot-datasource.password}")
    private String databasePassword;
    
    @Value("${spring.snapshot-datasource.driver-class-name}")
    private String databaseDriverClassName;
    
    @Value("${spring.snapshot-datasource.hikari.maximum-pool-size:5}")
    private int databaseMaxPoolSize;
    
    @Value("${spring.snapshot-datasource.hikari.minimum-idle:1}")
    private int databaseMinIdle;
    
    @Value("${spring.snapshot-datasource.hikari.connection-timeout:10000}")
    private long databaseConnectionTimeout;
    
    // Redis Configuration
    @Value("${spring.redis.host}")
    private String redisHost;
    
    @Value("${spring.redis.port}")
    private int redisPort;
    
    @Value("${spring.redis.password:}")
    private String redisPassword;
    
    @Value("${spring.redis.database}")
    private int redisDatabase;
    
    @Value("${spring.redis.timeout:2000ms}")
    private String redisTimeout;
    
    @Value("${spring.redis.lettuce.pool.max-active:8}")
    private int redisMaxPoolSize;
    
    @Value("${spring.redis.lettuce.pool.min-idle:0}")
    private int redisMinIdle;
    
    // Cache Configuration
    @Value("${cache.redis.key-prefix:user:}")
    private String redisKeyPrefix;
    
    @Value("${cache.redis.ttl:PT30M}")
    private Duration redisTtl;
    
    @Value("${cache.database.ttl:PT6H}")
    private Duration databaseTtl;
    
    @Value("${cache.user-service.base-url:http://localhost:8081}")
    private String userServiceBaseUrl;
    
    // Cleanup Configuration
    @Value("${user-cache.cleanup.cron:0 0 2 * * ?}")
    private String cleanupCron;
    
    @Value("${user-cache.cleanup.batch-size:100}")
    private int cleanupBatchSize;
}
