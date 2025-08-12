package com.andev.post.service.rsql;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for dynamically mapping entity field names to database column names for sorting.
 *
 * This class uses reflection to inspect entity fields and their JPA annotations to determine
 * the correct database column name for sorting. It includes caching for performance optimization.
 *
 * <h3>Mapping Strategy:</h3>
 * <ol>
 *   <li>Check for @Column annotation with explicit name</li>
 *   <li>Check for @JoinColumn annotation with explicit name</li>
 *   <li>Fall back to camelCase to snake_case conversion</li>
 * </ol>
 */
@Slf4j
public class SortingHelper {

    private static final Map<Class<?>, Map<String, String>> FIELD_MAPPING_CACHE = new ConcurrentHashMap<>();

    /**
     * Gets the database column name for a given entity field.
     *
     * @param entityClass The entity class
     * @param fieldName The field name to map
     * @return The database column name
     */
    public static String getColumnName(Class<?> entityClass, String fieldName) {
        Map<String, String> fieldMapping = FIELD_MAPPING_CACHE.computeIfAbsent(entityClass, k -> new HashMap<>());

        return fieldMapping.computeIfAbsent(fieldName, k -> {
            try {
                Field field = getField(entityClass, fieldName);
                if (field == null) {
                    log.warn("Field {} not found in class {}", fieldName, entityClass.getSimpleName());
                    return fieldName; // Return original name as fallback
                }

                // Check for @Column annotation with explicit name
                Column column = field.getAnnotation(Column.class);
                if (column != null && !column.name().isEmpty()) {
                    return column.name();
                }

                // Check for @JoinColumn annotation with explicit name
                JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                if (joinColumn != null && !joinColumn.name().isEmpty()) {
                    return joinColumn.name();
                }

                // Fall back to camelCase to snake_case conversion
                return camelCaseToSnakeCase(fieldName);

            } catch (Exception e) {
                log.warn(
                        "Error getting column name for field {} in class {}",
                        fieldName,
                        entityClass.getSimpleName(),
                        e);
                return fieldName; // Return original name as fallback
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
     * Converts camelCase string to snake_case.
     *
     * @param camelCase The camelCase string to convert
     * @return The snake_case string
     */
    private static String camelCaseToSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * Checks if a field exists in the entity class.
     *
     * @param entityClass The entity class to check
     * @param fieldName The name of the field to check
     * @return true if the field exists, false otherwise
     */
    public static boolean fieldExists(Class<?> entityClass, String fieldName) {
        return getField(entityClass, fieldName) != null;
    }

    /**
     * Clears the field mapping cache.
     * Useful for testing or when entity classes are reloaded.
     */
    public static void clearCache() {
        FIELD_MAPPING_CACHE.clear();
    }
}
