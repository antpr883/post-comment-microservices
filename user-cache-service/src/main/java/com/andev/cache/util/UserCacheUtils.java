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
        Object userId = userData.get(UserCacheFields.USER_ID);
        return userId instanceof Long ? (Long) userId : null;
    }

    /**
     * Extracts username from user data map.
     * 
     * @param userData user data map
     * @return username or null if not found
     */
    public static String extractUsername(Map<String, Object> userData) {
        Object username = userData.get(UserCacheFields.USERNAME);
        return username instanceof String ? (String) username : null;
    }

    /**
     * Validates user data map contains required fields.
     * 
     * @param userData user data map
     * @return true if valid, false otherwise
     */
    public static boolean isValidUserData(Map<String, Object> userData) {
        if (userData == null) {
            log.warn("User data is null");
            return false;
        }
        
        Long userId = extractUserId(userData);
        String username = extractUsername(userData);
        
        if (userId == null) {
            log.warn("User ID is missing or invalid in user data");
            return false;
        }
        
        if (username == null || username.trim().isEmpty()) {
            log.warn("Username is missing or empty in user data for userId: {}", userId);
            return false;
        }
        
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
} 