package com.andev.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for User Cache Service.
 * Supports both PostgreSQL snapshot database and Redis caching.
 */
@Data
@ConfigurationProperties(prefix = "user-cache")
public class UserCacheProperties {
    
    /**
     * Whether to enable the user cache service
     */
    private boolean enabled = true;
    
    /**
     * Database configuration for user snapshots
     */
    private Database database = new Database();
    
    /**
     * Redis configuration for caching
     */
    private Redis redis = new Redis();
    
    /**
     * Cache configuration
     */
    private Cache cache = new Cache();
    
    /**
     * Cleanup configuration
     */
    private Cleanup cleanup = new Cleanup();
    
    @Data
    public static class Database {
        private String url = "jdbc:postgresql://localhost:5432/user_snapshot?ssl=false";
        private String username = "snap_user";
        private String password = "1234";
        private String driverClassName = "org.postgresql.Driver";
        private Hikari hikari = new Hikari();
        
        @Data
        public static class Hikari {
            private int maximumPoolSize = 5;
            private int minimumIdle = 1;
            private long connectionTimeout = 10000;
        }
    }
    
    @Data
    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private String password = "";
        private int database = 0;
        private Duration timeout = Duration.ofSeconds(2);
        private Lettuce lettuce = new Lettuce();
        
        @Data
        public static class Lettuce {
            private Pool pool = new Pool();
            
            @Data
            public static class Pool {
                private int maxActive = 8;
                private int minIdle = 0;
            }
        }
    }
    
    @Data
    public static class Cache {
        private String redisKeyPrefix = "user:";
        private Duration redisTtl = Duration.ofMinutes(30);
        private Duration databaseTtl = Duration.ofHours(6);
        private String userServiceBaseUrl = "http://localhost:8081";
    }
    
    @Data
    public static class Cleanup {
        private String cron = "0 0 2 * * ?";
        private int batchSize = 100;
    }
}
