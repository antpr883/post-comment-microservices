package com.andev.cache.config.redis;

import com.andev.cache.config.UserCacheProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "user-cache.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    private final UserCacheProperties properties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        UserCacheProperties.Redis redisConfig = properties.getRedis();
        
        log.info("=== CREATING REDIS CONNECTION FACTORY ===");
        log.info("Redis host: {}", redisConfig.getHost());
        log.info("Redis port: {}", redisConfig.getPort());
        log.info("Redis database: {}", redisConfig.getDatabase());
        log.info("Redis timeout: {}", redisConfig.getTimeout());
        log.info("Redis password: {}", redisConfig.getPassword() != null && !redisConfig.getPassword().isEmpty() ? "***" : "none");
        
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisConfig.getHost());
        config.setPort(redisConfig.getPort());
        config.setDatabase(redisConfig.getDatabase());
        
        if (redisConfig.getPassword() != null && !redisConfig.getPassword().isEmpty()) {
            config.setPassword(redisConfig.getPassword());
        }
        
        log.info("Redis configuration created successfully");
        log.info("=== REDIS CONNECTION FACTORY CREATED ===");
        
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure serializers
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
} 