package com.andev.cache.service;

import java.util.Map;
import java.util.Optional;

/**
 * Сервіс для кешування даних користувачів.
 * Реалізує повне флоу кешування: Redis -> PostgreSQL -> External API
 * Повертає дані у вигляді Map для уникнення залежностей від конкретних DTO.
 */
public interface UserCacheService {
    
    /**
     * Отримує дані користувача за ID з повним флоу кешування.
     * 1. Перевіряє Redis (гарячий кеш)
     * 2. Перевіряє PostgreSQL (теплий кеш)
     * 3. Викликає зовнішнє API (холодні дані)
     * 
     * @param userId ID користувача
     * @return Optional з Map даних користувача або empty якщо не знайдено
     */
    Optional<Map<String, Object>> getUserById(Long userId);
    
    /**
     * Отримує дані користувача за username.
     * 
     * @param username ім'я користувача
     * @return Optional з Map даних користувача або empty якщо не знайдено
     */
    Optional<Map<String, Object>> getUserByUsername(String username);
    
    /**
     * Кешує дані користувача в Redis та PostgreSQL.
     * 
     * @param userData Map з даними користувача
     */
    void cacheUser(Map<String, Object> userData);
    
    /**
     * Видаляє користувача з Redis та PostgreSQL кешу.
     * 
     * @param userId ID користувача для видалення
     */
    void evictUser(Long userId);
    
    /**
     * Перевіряє чи є користувач в Redis кеші.
     * 
     * @param userId ID користувача
     * @return true якщо користувач є в Redis кеші, false інакше
     */
    boolean isUserCachedInRedis(Long userId);
    
    /**
     * Перевіряє чи є користувач в PostgreSQL кеші.
     * 
     * @param userId ID користувача
     * @return true якщо користувач є в PostgreSQL кеші, false інакше
     */
    boolean isUserCachedInDatabase(Long userId);
} 