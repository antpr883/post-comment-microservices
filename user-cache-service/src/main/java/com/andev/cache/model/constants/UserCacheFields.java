package com.andev.cache.model.constants;

/**
 * Константи для ключів Map в UserCacheService.
 * Використовуються для уніфікованого повернення даних користувача.
 */
public final class UserCacheFields {
    
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String CACHED_AT = "cachedAt";
    public static final String EXPIRES_AT = "expiresAt";
    
    private UserCacheFields() {

    }
} 