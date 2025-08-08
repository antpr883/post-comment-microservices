package com.andev.comment.service.rsql;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for dynamic field mapping in MongoDB entities.
 *
 * This class provides functionality to dynamically map entity field names
 * to MongoDB field names, supporting both explicit @Field annotations and
 * automatic camelCase to snake_case conversion.
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Annotation-aware:</strong> Uses @Field annotations for explicit mapping</li>
 *   <li><strong>Automatic conversion:</strong> Converts camelCase to snake_case when needed</li>
 *   <li><strong>Fallback support:</strong> Returns original field name if mapping fails</li>
 *   <li><strong>Performance optimized:</strong> Caches field mappings for efficiency</li>
 * </ul>
 *
 * <h3>Mapping Strategy:</h3>
 * <ol>
 *   <li><strong>Check @Field annotation:</strong> Use explicit field name if present</li>
 *   <li><strong>CamelCase conversion:</strong> Convert camelCase to snake_case</li>
 *   <li><strong>Fallback:</strong> Return original field name if conversion fails</li>
 * </ol>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Get MongoDB field name for entity field
 * String fieldName = SortingHelper.getColumnName(Comments.class, "userId");
 * // Returns: "userId" (no conversion needed for MongoDB)
 *
 * String fieldName2 = SortingHelper.getColumnName(Comments.class, "likesCount");
 * // Returns: "likesCount" (no conversion needed for MongoDB)
 * }</pre>
 *
 * <h3>Field Mapping Examples:</h3>
 * <ul>
 *   <li><strong>userId</strong> → "userId" (no conversion needed)</li>
 *   <li><strong>postId</strong> → "postId" (no conversion needed)</li>
 *   <li><strong>likesCount</strong> → "likesCount" (no conversion needed)</li>
 *   <li><strong>createdAt</strong> → "createdAt" (no conversion needed)</li>
 * </ul>
 *
 * @author Comment Service Team
 * @version 1.0
 * @since 2025-08-06
 */
public class SortingHelper {

    private static final Map<String, String> fieldMappingCache = new HashMap<>();

    /**
     * Gets the MongoDB field name for a given entity field.
     *
     * This method provides dynamic field mapping for MongoDB entities.
     * It checks for @Field annotations first, then falls back to
     * automatic camelCase to snake_case conversion.
     *
     * <h3>Mapping Process:</h3>
     * <ol>
     *   <li><strong>Cache check:</strong> Return cached mapping if available</li>
     *   <li><strong>Annotation check:</strong> Look for @Field annotation</li>
     *   <li><strong>Conversion:</strong> Apply camelCase to snake_case conversion</li>
     *   <li><strong>Cache result:</strong> Store mapping for future use</li>
     * </ol>
     *
     * @param entityClass The entity class to analyze
     * @param fieldName The field name to map
     * @return The MongoDB field name
     *
     * <h3>Examples:</h3>
     * <pre>{@code
     * // Basic field mapping
     * String field = SortingHelper.getColumnName(Comments.class, "userId");
     * // Returns: "userId"
     *
     * // Field with @Field annotation
     * String field2 = SortingHelper.getColumnName(Comments.class, "likesCount");
     * // Returns: "likesCount"
     * }</pre>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *   <li><strong>Field not found:</strong> Returns original field name</li>
     *   <li><strong>Reflection errors:</strong> Returns original field name</li>
     *   <li><strong>Null inputs:</strong> Returns original field name</li>
     * </ul>
     */
    public static String getColumnName(Class<?> entityClass, String fieldName) {
        if (entityClass == null || fieldName == null) {
            return fieldName;
        }

        String cacheKey = entityClass.getName() + "." + fieldName;

        // Check cache first
        if (fieldMappingCache.containsKey(cacheKey)) {
            return fieldMappingCache.get(cacheKey);
        }

        try {
            Field field = entityClass.getDeclaredField(fieldName);

            // Check for @Field annotation
            org.springframework.data.mongodb.core.mapping.Field fieldAnnotation =
                    field.getAnnotation(org.springframework.data.mongodb.core.mapping.Field.class);
            if (fieldAnnotation != null && !fieldAnnotation.value().isEmpty()) {
                String mappedField = fieldAnnotation.value();
                fieldMappingCache.put(cacheKey, mappedField);
                return mappedField;
            }

            // For MongoDB, we typically don't need conversion, but keep the logic for consistency
            String convertedField = convertCamelCaseToSnakeCase(fieldName);
            fieldMappingCache.put(cacheKey, convertedField);
            return convertedField;

        } catch (NoSuchFieldException e) {
            // Field not found, return original name
            fieldMappingCache.put(cacheKey, fieldName);
            return fieldName;
        } catch (Exception e) {
            // Any other error, return original name
            fieldMappingCache.put(cacheKey, fieldName);
            return fieldName;
        }
    }

    /**
     * Converts camelCase field names to snake_case.
     *
     * This method provides automatic conversion from Java camelCase
     * naming convention to database snake_case convention.
     *
     * <h3>Conversion Rules:</h3>
     * <ul>
     *   <li><strong>Single word:</strong> No conversion needed</li>
     *   <li><strong>Multiple words:</strong> Insert underscore before capital letters</li>
     *   <li><strong>Acronyms:</strong> Preserved as-is</li>
     * </ul>
     *
     * <h3>Examples:</h3>
     * <ul>
     *   <li><strong>userId</strong> → "user_id"</li>
     *   <li><strong>postId</strong> → "post_id"</li>
     *   <li><strong>likesCount</strong> → "likes_count"</li>
     *   <li><strong>createdAt</strong> → "created_at"</li>
     *   <li><strong>updatedAt</strong> → "updated_at"</li>
     * </ul>
     *
     * @param camelCase The camelCase string to convert
     * @return The snake_case equivalent
     */
    private static String convertCamelCaseToSnakeCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }

        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(camelCase.charAt(0)));

        for (int i = 1; i < camelCase.length(); i++) {
            char ch = camelCase.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append('_');
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }

    /**
     * Clears the field mapping cache.
     *
     * This method can be used to clear the internal cache if needed,
     * for example during testing or when entity structures change.
     */
    public static void clearCache() {
        fieldMappingCache.clear();
    }
}
