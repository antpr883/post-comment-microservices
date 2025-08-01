package com.andev.cache.service.impl;

import com.andev.cache.config.UserCacheProperties;
import com.andev.cache.service.UserHubClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserHubClientImpl implements UserHubClient {

    private final RestTemplate restTemplate;
    private final UserCacheProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<Map<String, Object>> fetchUserById(Long userId) {
        try {
            String url = properties.getCache().getUserServiceBaseUrl() + "/api/users/" + userId;
            log.debug("Fetching user from external service: {}", url);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("User fetched successfully from external service: {}", userId);
                return Optional.of(response.getBody());
            }
            
            log.debug("User not found in external service: {}", userId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching user from external service: {}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Map<String, Object>> fetchUserByUsername(String username) {
        try {
            String url = properties.getCache().getUserServiceBaseUrl() + "/api/users/username/" + username;
            log.debug("Fetching user by username from external service: {}", url);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("User fetched successfully by username from external service: {}", username);
                return Optional.of(response.getBody());
            }
            
            log.debug("User not found by username in external service: {}", username);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching user by username from external service: {}", username, e);
            return Optional.empty();
        }
    }
} 