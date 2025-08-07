package com.andev.post.service.rsql;

import java.util.List;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import jakarta.persistence.criteria.*;

public class JsonSupport {

    public static Predicate buildJsonPredicate(
            Root<?> root, CriteriaBuilder cb, String selector, ComparisonOperator operator, List<String> args) {
        String[] parts = selector.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Only 1-level JSON path supported (e.g. 'description.author')");
        }

        String jsonField = parts[0];
        String jsonKey = parts[1];
        String value = args.get(0);

        Expression<String> jsonExtract =
                cb.function("jsonb_extract_path_text", String.class, root.get(jsonField), cb.literal(jsonKey));

        return switch (operator.getSymbol()) {
            case "==" -> {
                if (value.startsWith("*") && value.endsWith("*")) {
                    // LIKE pattern: *value* -> %value%
                    String pattern = "%" + value.substring(1, value.length() - 1) + "%";
                    yield cb.like(cb.lower(jsonExtract), pattern.toLowerCase());
                } else if (value.startsWith("*")) {
                    // LIKE pattern: *value -> %value
                    String pattern = "%" + value.substring(1);
                    yield cb.like(cb.lower(jsonExtract), pattern.toLowerCase());
                } else if (value.endsWith("*")) {
                    // LIKE pattern: value* -> value%
                    String pattern = value.substring(0, value.length() - 1) + "%";
                    yield cb.like(cb.lower(jsonExtract), pattern.toLowerCase());
                } else {
                    // Exact match
                    yield cb.equal(jsonExtract, value);
                }
            }
            case "!=" -> cb.notEqual(jsonExtract, value);
            case "=in=" -> {
                // IN operator for JSON fields
                CriteriaBuilder.In<String> inClause = cb.in(jsonExtract);
                args.forEach(inClause::value);
                yield inClause;
            }
            case "=out=" -> {
                // NOT IN operator for JSON fields
                CriteriaBuilder.In<String> inClause = cb.in(jsonExtract);
                args.forEach(inClause::value);
                yield cb.not(inClause);
            }
            default -> throw new UnsupportedOperationException("Operator not supported for JSON: " + operator);
        };
    }

    public static Predicate buildStandardPredicate(
            CriteriaBuilder cb, Path<?> path, Object value, ComparisonOperator operator) {
        // Перевірка, що тип підтримує порівняння
        if (!Comparable.class.isAssignableFrom(path.getJavaType())) {
            throw new IllegalArgumentException("Field is not comparable: " + path.getJavaType());
        }
        if (!(value instanceof Comparable)) {
            throw new IllegalArgumentException("Value is not comparable: " + value);
        }

        Class<?> javaType = path.getJavaType();

        switch (operator.getSymbol()) {
            case "==" -> {
                if (value instanceof String stringValue) {
                    if (stringValue.startsWith("*") && stringValue.endsWith("*")) {
                        // LIKE pattern: *value* -> %value%
                        String pattern = "%" + stringValue.substring(1, stringValue.length() - 1) + "%";
                        return cb.like(cb.lower(path.as(String.class)), pattern.toLowerCase());
                    } else if (stringValue.startsWith("*")) {
                        // LIKE pattern: *value -> %value
                        String pattern = "%" + stringValue.substring(1);
                        return cb.like(cb.lower(path.as(String.class)), pattern.toLowerCase());
                    } else if (stringValue.endsWith("*")) {
                        // LIKE pattern: value* -> value%
                        String pattern = stringValue.substring(0, stringValue.length() - 1) + "%";
                        return cb.like(cb.lower(path.as(String.class)), pattern.toLowerCase());
                    }
                }
                return cb.equal(path, value);
            }
            case "!=" -> {
                return cb.notEqual(path, value);
            }
            case ">" -> {
                return greaterThanSafe(cb, path, value, javaType);
            }
            case ">=" -> {
                return greaterThanOrEqualToSafe(cb, path, value, javaType);
            }
            case "<" -> {
                return lessThanSafe(cb, path, value, javaType);
            }
            case "<=" -> {
                return lessThanOrEqualToSafe(cb, path, value, javaType);
            }
            case "=in=" -> {
                // IN operator
                CriteriaBuilder.In<Object> inClause = cb.in(path);
                if (value instanceof List<?> list) {
                    list.forEach(inClause::value);
                } else {
                    inClause.value(value);
                }
                return inClause;
            }
            case "=out=" -> {
                // NOT IN operator
                CriteriaBuilder.In<Object> inClause = cb.in(path);
                if (value instanceof List<?> list) {
                    list.forEach(inClause::value);
                } else {
                    inClause.value(value);
                }
                return cb.not(inClause);
            }
            default -> throw new UnsupportedOperationException("Operator not supported: " + operator);
        }
    }

    // ======= Type-safe wrappers ========
    private static <Y extends Comparable<? super Y>> Predicate greaterThanSafe(
            CriteriaBuilder cb, Path<?> path, Object value, Class<?> type) {
        return cb.greaterThan(((Path<Y>) path).as((Class<Y>) type), (Y) value);
    }

    private static <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualToSafe(
            CriteriaBuilder cb, Path<?> path, Object value, Class<?> type) {
        return cb.greaterThanOrEqualTo(((Path<Y>) path).as((Class<Y>) type), (Y) value);
    }

    private static <Y extends Comparable<? super Y>> Predicate lessThanSafe(
            CriteriaBuilder cb, Path<?> path, Object value, Class<?> type) {
        return cb.lessThan(((Path<Y>) path).as((Class<Y>) type), (Y) value);
    }

    private static <Y extends Comparable<? super Y>> Predicate lessThanOrEqualToSafe(
            CriteriaBuilder cb, Path<?> path, Object value, Class<?> type) {
        return cb.lessThanOrEqualTo(((Path<Y>) path).as((Class<Y>) type), (Y) value);
    }

    public static Object castToRequiredType(Class<?> type, String value) {
        if (Enum.class.isAssignableFrom(type)) {
            return Enum.valueOf((Class<Enum>) type, value);
        } else if (type == Long.class || type == long.class) {
            return Long.valueOf(value);
        } else if (type == Integer.class || type == int.class) {
            return Integer.valueOf(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return Boolean.valueOf(value);
        } else if (type == Double.class || type == double.class) {
            return Double.valueOf(value);
        } else {
            return value;
        }
    }

    /**
     * Builds a predicate for automatic JSON field search.
     *
     * This method handles RSQL queries where a JSON field is referenced without a specific key.
     * For example: description==*first* will search in the 'summary' key of the description field.
     *
     * @param root The root entity
     * @param cb The criteria builder
     * @param jsonField The JSON field name
     * @param operator The comparison operator
     * @param args The arguments for the comparison
     * @return The predicate for the JSON field search
     */
    public static Predicate buildAutoJsonPredicate(
            Root<?> root, CriteriaBuilder cb, String jsonField, ComparisonOperator operator, List<String> args) {
        String value = args.get(0);

        // For the description field, search in the 'summary' key
        Expression<String> jsonExtract =
                cb.function("jsonb_extract_path_text", String.class, root.get(jsonField), cb.literal("summary"));

        return switch (operator.getSymbol()) {
            case "==" -> {
                if (value.startsWith("*") && value.endsWith("*")) {
                    String pattern = "%" + value.substring(1, value.length() - 1) + "%";
                    yield cb.like(cb.lower(jsonExtract), pattern.toLowerCase());
                } else if (value.startsWith("*")) {
                    String pattern = "%" + value.substring(1);
                    yield cb.like(cb.lower(jsonExtract), pattern.toLowerCase());
                } else if (value.endsWith("*")) {
                    String pattern = value.substring(0, value.length() - 1) + "%";
                    yield cb.like(cb.lower(jsonExtract), pattern.toLowerCase());
                } else {
                    yield cb.equal(jsonExtract, value);
                }
            }
            case "!=" -> {
                yield cb.notEqual(jsonExtract, value);
            }
            case "=in=" -> {
                CriteriaBuilder.In<String> inClause = cb.in(jsonExtract);
                args.forEach(inClause::value);
                yield inClause;
            }
            case "=out=" -> {
                CriteriaBuilder.In<String> inClause = cb.in(jsonExtract);
                args.forEach(inClause::value);
                yield cb.not(inClause);
            }
            default -> throw new UnsupportedOperationException("Operator not supported for auto JSON: " + operator);
        };
    }
}
