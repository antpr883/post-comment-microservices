package com.andev.cache.config;


import com.andev.cache.client.UserHubClient;
import com.andev.cache.config.jpa.SnapshotRepositoryConfiguration;
import com.andev.cache.repository.snapshot.UserSnapshotRepository;
import com.andev.cache.service.DatabaseUserCacheService;
import com.andev.cache.service.RedisUserCacheService;
import com.andev.cache.service.UserCacheService;
import com.andev.cache.service.impl.DatabaseUserCacheServiceImpl;
import com.andev.cache.service.impl.RedisUserCacheServiceImpl;
import com.andev.cache.service.impl.UserCacheServiceImpl;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@Slf4j
@AutoConfiguration
@ConditionalOnProperty(
        name = "user-cache.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableScheduling
@Import({SnapshotRepositoryConfiguration.class})
@ComponentScan(basePackages = "com.andev.cache")
public class CacheServiceAutoConfiguration {

    @Bean(name = "snapshotDataSource")
    @ConditionalOnMissingBean(name = "snapshotDataSource")
    @ConditionalOnProperty(name = "user-cache.enabled", havingValue = "true", matchIfMissing = true)
    public DataSource snapshotDataSource(CacheServiceProperties properties) {
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setJdbcUrl(properties.getDatabaseUrl());
        dataSource.setUsername(properties.getDatabaseUsername());
        dataSource.setPassword(properties.getDatabasePassword());
        dataSource.setDriverClassName(properties.getDatabaseDriverClassName());
        dataSource.setMaximumPoolSize(properties.getDatabaseMaxPoolSize());
        dataSource.setMinimumIdle(properties.getDatabaseMinIdle());
        dataSource.setConnectionTimeout(properties.getDatabaseConnectionTimeout());

        return dataSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisUserCacheService redisUserCacheService(
            RedisTemplate<String, Object> redisTemplate,
            CacheServiceProperties properties) {
        return new RedisUserCacheServiceImpl(redisTemplate, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public DatabaseUserCacheService databaseUserCacheService(
            UserSnapshotRepository userSnapshotRepository,
            CacheServiceProperties properties) {
        return new DatabaseUserCacheServiceImpl(userSnapshotRepository, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserCacheService userCacheService(
            RedisUserCacheService redisUserCacheService,
            DatabaseUserCacheService databaseUserCacheService,
            UserHubClient userHubClient,
            UserSnapshotRepository userSnapshotRepository) {
        return new UserCacheServiceImpl(redisUserCacheService, databaseUserCacheService, userHubClient, userSnapshotRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserHubClient userHubClient(RestTemplate restTemplate, CacheServiceProperties properties) {
        return new UserHubClient(restTemplate, properties.getUserServiceBaseUrl());
    }

    @Bean
    public UserCacheInitializer userCacheInitializer(CacheServiceProperties properties) {
        return new UserCacheInitializer(properties);
    }

    public static class UserCacheInitializer {
        public UserCacheInitializer(CacheServiceProperties properties) {
            log.info("Initializing user cache service with Redis and PostgreSQL support");
            log.info("Database: {}", properties.getDatabaseUrl());
            log.info("Redis: {}:{}", properties.getRedisHost(), properties.getRedisPort());
            log.info("User Service: {}", properties.getUserServiceBaseUrl());
        }
    }
}
