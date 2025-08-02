package com.andev.cache.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Client for making API calls to User Hub service.
 * Fetches user data from external service when not found in cache.
 */
@Slf4j
@Component
public class UserHubClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UserHubClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Fetches user summary from User Hub service.
     * 
     * @param userId ID of the user to fetch
     * @return Optional with user data or empty if not found/error
     */
    public Optional<Map<String, Object>> getUserSummary(Long userId) {
        try {
            log.debug("🌐 Fetching user summary from User Hub for userId: {}", userId);
            
            String url = baseUrl + "/api/users/" + userId;
            UserSummary response = restTemplate.getForObject(url, UserSummary.class);
            
            if (response != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", response.getId());
                userData.put("username", response.getUsername());
                
                log.debug("✅ Successfully fetched user summary for userId: {}", userId);
                return Optional.of(userData);
            } else {
                log.warn("⚠️ User Hub returned null response for userId: {}", userId);
                return Optional.empty();
            }
            
        } catch (ResourceAccessException e) {
            log.error("❌ Failed to connect to User Hub service for userId {}: {}", userId, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("❌ Error fetching user summary for userId {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * DTO for User Hub API response.
     */
    public static class UserSummary {
        private Long id;
        private String username;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
} 