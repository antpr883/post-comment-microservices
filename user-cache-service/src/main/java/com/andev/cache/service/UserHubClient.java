package com.andev.cache.service;

import java.util.Map;
import java.util.Optional;

public interface UserHubClient {

    /**
     * Fetch user data from external user service
     */
    Optional<Map<String, Object>> fetchUserById(Long userId);

    /**
     * Fetch user data by username from external user service
     */
    Optional<Map<String, Object>> fetchUserByUsername(String username);
} 