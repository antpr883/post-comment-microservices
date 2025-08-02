package com.andev.cache.util;


import com.andev.cache.model.constants.UserCacheFields;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Utility class for user cache specific operations.
 */
@Slf4j
public final class UserCacheUtils {

    private UserCacheUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Extracts user ID from user data map.
     * 
     * @param userData user data map
     * @return user ID or null if not found
     */
    public static Long extractUserId(Map<String, Object> userData) {
        if (userData == null) {
            log.warn("❌ Cannot extract user ID from null user data");
            return null;
        }
        
        Object userId = userData.get(UserCacheFields.USER_ID);
        if (userId == null) {
            log.warn("❌ User ID is null in user data");
            return null;
        }
        
        if (userId instanceof Long) {
            return (Long) userId;
        } else if (userId instanceof Number) {
            return ((Number) userId).longValue();
        } else {
            log.warn("❌ User ID is not a valid number: {}", userId);
            return null;
        }
    }

    /**
     * Extracts username from user data map.
     * 
     * @param userData user data map
     * @return username or null if not found
     */
    public static String extractUsername(Map<String, Object> userData) {
        if (userData == null) {
            log.warn("❌ Cannot extract username from null user data");
            return null;
        }
        
        Object username = userData.get(UserCacheFields.USERNAME);
        if (username == null) {
            log.warn("❌ Username is null in user data");
            return null;
        }
        
        if (username instanceof String) {
            String usernameStr = (String) username;
            if (usernameStr.trim().isEmpty()) {
                log.warn("❌ Username is empty in user data");
                return null;
            }
            return usernameStr;
        } else {
            log.warn("❌ Username is not a string: {}", username);
            return null;
        }
    }

    /**
     * Validates user data map contains required fields.
     * 
     * @param userData user data map
     * @return true if valid, false otherwise
     */
    public static boolean isValidUserData(Map<String, Object> userData) {
        if (userData == null) {
            log.warn("❌ User data is null");
            return false;
        }
        
        Long userId = extractUserId(userData);
        String username = extractUsername(userData);
        
        if (userId == null) {
            log.warn("❌ User ID is missing or invalid in user data");
            return false;
        }
        
        if (username == null || username.trim().isEmpty()) {
            log.warn("❌ Username is missing or empty in user data for userId: {}", userId);
            return false;
        }
        
        log.debug("✅ User data validation passed for userId: {}, username: {}", userId, username);
        return true;
    }

    /**
     * Creates a standardized log message for cache operations.
     * 
     * @param operation operation name
     * @param userId user ID
     * @param source cache source
     * @return formatted log message
     */
    public static String createLogMessage(String operation, Long userId, String source) {
        return String.format("%s user %d from %s", operation, userId, source);
    }

    /**
     * Safely extracts a field from user data map.
     * 
     * @param userData user data map
     * @param fieldName field name to extract
     * @param defaultValue default value if field is not found
     * @return field value or default value
     */
    public static <T> T extractField(Map<String, Object> userData, String fieldName, T defaultValue) {
        if (userData == null || fieldName == null) {
            log.debug("❌ Cannot extract field '{}' from null user data", fieldName);
            return defaultValue;
        }
        
        Object value = userData.get(fieldName);
        if (value == null) {
            log.debug("❌ Field '{}' is null in user data", fieldName);
            return defaultValue;
        }
        
        try {
            @SuppressWarnings("unchecked")
            T result = (T) value;
            return result;
        } catch (ClassCastException e) {
            log.warn("❌ Field '{}' has wrong type: expected {}, got {}", 
                    fieldName, defaultValue != null ? defaultValue.getClass().getSimpleName() : "null", 
                    value.getClass().getSimpleName());
            return defaultValue;
        }
    }
} 