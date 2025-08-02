package com.andev.cache.config.web;


import com.andev.cache.client.UserHubClient;
import com.andev.cache.config.CacheServiceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Web configuration for User Cache Service.
 * Provides RestTemplate for external API calls.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "user-cache.enabled", havingValue = "true", matchIfMissing = true)
public class WebConfig {

    private final CacheServiceProperties properties;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public UserHubClient userHubClient(RestTemplate restTemplate) {
        return new UserHubClient(restTemplate, properties.getUserServiceBaseUrl());
    }
} 