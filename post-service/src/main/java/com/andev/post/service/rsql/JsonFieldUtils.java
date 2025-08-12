package com.andev.post.service.rsql;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for detecting JSON fields in entities using reflection.
 *
 * This class provides methods to determine if a field is a JSON type by checking:
 * - If the field type is JsonNode
 * - If the field has @JdbcTypeCode(SqlTypes.JSON) annotation
 * - If the field has @Column(columnDefinition = "jsonb") annotation
 *
 * The class includes caching for performance optimization.
 */
@Slf4j
public class JsonFieldUtils {

    private static final Map<Class<?>, Map<String, Boolean>> JSON_FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * Checks if a field in an entity class is a JSON field.
     *
     * @param entityClass The entity class to check
     * @param fieldName The name of the field to check
     * @return true if the field is a JSON field, false otherwise
     */
    public static boolean isJsonField(Class<?> entityClass, String fieldName) {
        Map<String, Boolean> fieldCache = JSON_FIELD_CACHE.computeIfAbsent(entityClass, k -> new HashMap<>());

        return fieldCache.computeIfAbsent(fieldName, k -> {
            try {
                Field field = getField(entityClass, fieldName);
                if (field == null) {
                    return false;
                }

                // Check if field type is JsonNode
                if (JsonNode.class.isAssignableFrom(field.getType())) {
                    return true;
                }

                // Check for @JdbcTypeCode(SqlTypes.JSON) annotation
                JdbcTypeCode jdbcTypeCode = field.getAnnotation(JdbcTypeCode.class);
                if (jdbcTypeCode != null && jdbcTypeCode.value() == SqlTypes.JSON) {
                    return true;
                }

                // Check for @Column(columnDefinition = "jsonb") annotation
                jakarta.persistence.Column column = field.getAnnotation(jakarta.persistence.Column.class);
                if (column != null
                        && column.columnDefinition() != null
                        && column.columnDefinition().toLowerCase().contains("jsonb")) {
                    return true;
                }

                return false;
            } catch (Exception e) {
                log.warn("Error checking if field {} is JSON in class {}", fieldName, entityClass.getSimpleName(), e);
                return false;
            }
        });
    }

    /**
     * Gets a field from a class, including superclass fields.
     *
     * @param clazz The class to search in
     * @param fieldName The name of the field to find
     * @return The Field object, or null if not found
     */
    private static Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return getField(superClass, fieldName);
            }
        }
        return null;
    }

    /**
     * Clears the JSON field cache.
     * Useful for testing or when entity classes are reloaded.
     */
    public static void clearCache() {
        JSON_FIELD_CACHE.clear();
    }
}
