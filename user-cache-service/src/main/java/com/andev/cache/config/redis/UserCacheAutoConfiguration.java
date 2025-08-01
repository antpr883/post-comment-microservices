package com.andev.cache.config.redis;

import com.andev.cache.config.UserCacheProperties;
import com.andev.cache.config.jpa.SnapshotRepositoryConfiguration;
import com.andev.cache.repository.UserSnapshotRepository;
import com.andev.cache.service.DatabaseUserCacheService;
import com.andev.cache.service.RedisUserCacheService;
import com.andev.cache.service.UserCacheService;
import com.andev.cache.service.UserHubClient;
import com.andev.cache.service.impl.DatabaseUserCacheServiceImpl;
import com.andev.cache.service.impl.RedisUserCacheServiceImpl;
import com.andev.cache.service.impl.UserCacheServiceImpl;
import com.andev.cache.service.impl.UserHubClientImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@Slf4j
@AutoConfiguration
@ConditionalOnProperty(
        name = "user-cache.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableConfigurationProperties(UserCacheProperties.class)
@EnableScheduling
@Import({SnapshotRepositoryConfiguration.class, RedisConfig.class})
@ComponentScan(basePackages = "com.andev.cache.service")
public class UserCacheAutoConfiguration {

    @Bean(name = "snapshotDataSource")
    @ConditionalOnMissingBean(name = "snapshotDataSource")
    @ConditionalOnProperty(name = "user-cache.enabled", havingValue = "true", matchIfMissing = true)
    public DataSource snapshotDataSource(@Validated UserCacheProperties properties) {
        UserCacheProperties.Database dbConfig = properties.getDatabase();
        
        log.info("Creating HikariDataSource with URL: {}", dbConfig.getUrl());
        
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dbConfig.getUrl());
        dataSource.setUsername(dbConfig.getUsername());
        dataSource.setPassword(dbConfig.getPassword());
        dataSource.setDriverClassName(dbConfig.getDriverClassName());
        dataSource.setMaximumPoolSize(dbConfig.getHikari().getMaximumPoolSize());
        dataSource.setMinimumIdle(dbConfig.getHikari().getMinimumIdle());
        dataSource.setConnectionTimeout(dbConfig.getHikari().getConnectionTimeout());
        dataSource.setPoolName("UserCacheHikariPool");

        return dataSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public com.andev.cache.model.domain.mapper.UserMapper userMapper() {
        return com.andev.cache.model.domain.mapper.UserMapper.INSTANCE;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisUserCacheService redisUserCacheService(
            RedisTemplate<String, Object> redisTemplate,
            UserCacheProperties properties) {
        return new RedisUserCacheServiceImpl(redisTemplate, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public DatabaseUserCacheService databaseUserCacheService(
            UserSnapshotRepository userSnapshotRepository,
            UserCacheProperties properties) {
        return new DatabaseUserCacheServiceImpl(userSnapshotRepository, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserHubClient userHubClient(
            RestTemplate restTemplate,
            UserCacheProperties properties,
            ObjectMapper objectMapper) {
        return new UserHubClientImpl(restTemplate, properties, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserCacheService userCacheService(
            RedisUserCacheService redisUserCacheService,
            DatabaseUserCacheService databaseUserCacheService,
            UserHubClient userHubClient){
        return new UserCacheServiceImpl(redisUserCacheService, databaseUserCacheService, userHubClient);
    }

    @Bean
    public UserCacheInitializer userCacheInitializer(UserCacheProperties properties) {
        return new UserCacheInitializer(properties);
    }

    public static class UserCacheInitializer {
        public UserCacheInitializer(UserCacheProperties properties) {
            log.info("Initializing user cache service with Redis and PostgreSQL support");
            log.info("Database: {}", properties.getDatabase().getUrl());
            log.info("Redis: {}:{}", properties.getRedis().getHost(), properties.getRedis().getPort());
            log.info("User Service: {}", properties.getCache().getUserServiceBaseUrl());
        }
    }
}
