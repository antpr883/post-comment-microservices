package com.andev.comment.service.rsql;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.OrNode;

/**
 * RSQL visitor implementation for MongoDB queries.
 *
 * This class implements the visitor pattern to traverse RSQL parse trees
 * and convert them into MongoDB queries. It handles all RSQL operators
 * and logical combinations.
 *
 * <h3>Supported Operators:</h3>
 * <ul>
 *   <li><strong>==</strong> - Equal (with wildcard support for strings)</li>
 *   <li><strong>!=</strong> - Not equal</li>
 *   <li><strong>></strong> - Greater than</li>
 *   <li><strong>>=</strong> - Greater than or equal</li>
 *   <li><strong><</strong> - Less than</li>
 *   <li><strong><=</strong> - Less than or equal</li>
 *   <li><strong>=in=</strong> - IN operator (multiple values)</li>
 *   <li><strong>=out=</strong> - NOT IN operator</li>
 * </ul>
 *
 * <h3>Logical Operators:</h3>
 * <ul>
 *   <li><strong>;</strong> - AND (logical AND)</li>
 *   <li><strong>,</strong> - OR (logical OR)</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Simple comparison
 * "content==*test*" → { "content": { "$regex": "test", "$options": "i" } }
 *
 * // Multiple conditions (AND)
 * "content==*test*;userId==user456" → { "content": { "$regex": "test", "$options": "i" }, "userId": "user456" }
 *
 * // Multiple conditions (OR)
 * "content==*test*,likesCount>5" → { "$or": [{ "content": { "$regex": "test", "$options": "i" } }, { "likesCount": { "$gt": 5 } }] }
 *
 * // Complex query
 * "(content==*test*;userId==user456),(likesCount>5)" → { "$or": [{ "content": { "$regex": "test", "$options": "i" }, "userId": "user456" }, { "likesCount": { "$gt": 5 } }] }
 * }</pre>
 *
 * @param <T> The entity type for this visitor
 * @author Comment Service Team
 * @version 1.0
 * @since 2025-08-06
 */
public class RsqlVisitorImpl<T> implements cz.jirutka.rsql.parser.ast.RSQLVisitor<Query, Class<T>> {

    private static final Logger log = LoggerFactory.getLogger(RsqlVisitorImpl.class);
    private final Class<T> entityClass;

    public RsqlVisitorImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Query visit(AndNode node, Class<T> entityClass) {
        log.debug("Processing AND node with {} children", node.getChildren().size());

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        for (cz.jirutka.rsql.parser.ast.Node child : node.getChildren()) {
            Query childQuery = child.accept(this, entityClass);
            if (childQuery.getQueryObject() != null
                    && !childQuery.getQueryObject().isEmpty()) {
                criteriaList.addAll(childQuery.getQueryObject().entrySet().stream()
                        .map(entry -> Criteria.where(entry.getKey()).is(entry.getValue()))
                        .collect(Collectors.toList()));
            }
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        return query;
    }

    @Override
    public Query visit(OrNode node, Class<T> entityClass) {
        log.debug("Processing OR node with {} children", node.getChildren().size());

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        for (cz.jirutka.rsql.parser.ast.Node child : node.getChildren()) {
            Query childQuery = child.accept(this, entityClass);
            if (childQuery.getQueryObject() != null
                    && !childQuery.getQueryObject().isEmpty()) {
                criteriaList.addAll(childQuery.getQueryObject().entrySet().stream()
                        .map(entry -> Criteria.where(entry.getKey()).is(entry.getValue()))
                        .collect(Collectors.toList()));
            }
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().orOperator(criteriaList.toArray(new Criteria[0])));
        }

        return query;
    }

    @Override
    public Query visit(ComparisonNode node, Class<T> entityClass) {
        String selector = node.getSelector();
        ComparisonOperator operator = node.getOperator();
        List<String> arguments = node.getArguments();

        log.debug("Processing comparison: {} {} {}", selector, operator.getSymbol(), arguments);

        Query query = new Query();
        Criteria criteria = buildCriteria(selector, operator, arguments);
        query.addCriteria(criteria);

        log.debug("Comparison query result: {}", query.getQueryObject());
        return query;
    }

    /**
     * Builds MongoDB criteria based on RSQL operator and arguments.
     *
     * This method converts RSQL comparison operators into MongoDB criteria.
     * It handles different data types and special cases like wildcard matching.
     *
     * <h3>Operator Mapping:</h3>
     * <ul>
     *   <li><strong>==</strong> → $eq (with wildcard support)</li>
     *   <li><strong>!=</strong> → $ne</li>
     *   <li><strong>></strong> → $gt</li>
     *   <li><strong>>=</strong> → $gte</li>
     *   <li><strong><</strong> → $lt</li>
     *   <li><strong><=</strong> → $lte</li>
     *   <li><strong>=in=</strong> → $in</li>
     *   <li><strong>=out=</strong> → $nin</li>
     * </ul>
     *
     * <h3>Wildcard Support:</h3>
     * <ul>
     *   <li><strong>*value*</strong> - Contains value (case-insensitive)</li>
     *   <li><strong>*value</strong> - Ends with value</li>
     *   <li><strong>value*</strong> - Starts with value</li>
     * </ul>
     *
     * @param selector The field name
     * @param operator The RSQL comparison operator
     * @param arguments The comparison arguments
     * @return MongoDB criteria
     */
    private Criteria buildCriteria(String selector, ComparisonOperator operator, List<String> arguments) {
        if (arguments.isEmpty()) {
            throw new IllegalArgumentException("No arguments provided for operator: " + operator);
        }

        String argument = arguments.get(0);

        switch (operator.getSymbol()) {
            case "==":
            case "=eq=":
                return buildEqualCriteria(selector, argument);
            case "!=":
            case "=ne=":
                return buildNotEqualCriteria(selector, argument);
            case ">":
            case "=gt=":
                return buildGreaterThanCriteria(selector, argument);
            case ">=":
            case "=gte=":
            case "=ge=":
                return buildGreaterThanOrEqualCriteria(selector, argument);
            case "<":
            case "=lt=":
                return buildLessThanCriteria(selector, argument);
            case "<=":
            case "=lte=":
            case "=le=":
                return buildLessThanOrEqualCriteria(selector, argument);
            case "=in=":
                return buildInCriteria(selector, arguments);
            case "=out=":
            case "=nin=":
                return buildNotInCriteria(selector, arguments);
            default:
                log.error("Unsupported RSQL operator: {}", operator.getSymbol());
                throw new IllegalArgumentException("Unsupported operator: " + operator.getSymbol()
                        + ". Supported operators: ==, !=, >, >=, <, <=, =in=, =out=");
        }
    }

    private Criteria buildEqualCriteria(String selector, String argument) {
        // Handle null values
        if ("null".equalsIgnoreCase(argument)) {
            log.debug("Building null check for {}: {}", selector, argument);
            return Criteria.where(selector).is(null);
        }

        // Handle boolean values
        if ("isDeleted".equals(selector)) {
            boolean value = Boolean.parseBoolean(argument);
            log.debug("Building boolean check for {}: {}", selector, value);
            return Criteria.where(selector).is(value);
        }

        // Handle string patterns
        if (argument.startsWith("*") && argument.endsWith("*")) {
            // Contains pattern: *value*
            String value = argument.substring(1, argument.length() - 1);
            log.debug("Building contains regex for {}: {}", selector, value);
            return Criteria.where(selector).regex(value, "i");
        } else if (argument.startsWith("*")) {
            // Ends with pattern: *value
            String value = argument.substring(1);
            log.debug("Building ends with regex for {}: {}", selector, value);
            return Criteria.where(selector).regex(value + "$", "i");
        } else if (argument.endsWith("*")) {
            // Starts with pattern: value*
            String value = argument.substring(0, argument.length() - 1);
            log.debug("Building starts with regex for {}: {}", selector, value);
            return Criteria.where(selector).regex("^" + value, "i");
        } else {
            // Exact match with proper type conversion
            Object convertedValue = convertValue(selector, argument);
            log.debug("Building exact match for {}: {}", selector, convertedValue);
            return Criteria.where(selector).is(convertedValue);
        }
    }

    private Criteria buildNotEqualCriteria(String selector, String argument) {
        // Handle null values
        if ("null".equalsIgnoreCase(argument)) {
            log.debug("Building not null check for {}: {}", selector, argument);
            return Criteria.where(selector).ne(null);
        }

        Object convertedValue = convertValue(selector, argument);
        log.debug("Building not equal for {}: {}", selector, convertedValue);
        return Criteria.where(selector).ne(convertedValue);
    }

    private Criteria buildGreaterThanCriteria(String selector, String argument) {
        Object convertedValue = convertValue(selector, argument);
        log.debug("Building greater than for {}: {}", selector, convertedValue);
        if (convertedValue instanceof Number) {
            return Criteria.where(selector).gt(convertedValue);
        } else {
            // For string comparison, use regex
            return Criteria.where(selector).regex(argument, "i");
        }
    }

    private Criteria buildGreaterThanOrEqualCriteria(String selector, String argument) {
        Object convertedValue = convertValue(selector, argument);
        log.debug("Building greater than or equal for {}: {}", selector, convertedValue);
        if (convertedValue instanceof Number) {
            return Criteria.where(selector).gte(convertedValue);
        } else {
            // For string comparison, use regex
            return Criteria.where(selector).regex(argument, "i");
        }
    }

    private Criteria buildLessThanCriteria(String selector, String argument) {
        Object convertedValue = convertValue(selector, argument);
        log.debug("Building less than for {}: {}", selector, convertedValue);
        if (convertedValue instanceof Number) {
            return Criteria.where(selector).lt(convertedValue);
        } else {
            // For string comparison, use regex
            return Criteria.where(selector).regex(argument, "i");
        }
    }

    private Criteria buildLessThanOrEqualCriteria(String selector, String argument) {
        Object convertedValue = convertValue(selector, argument);
        log.debug("Building less than or equal for {}: {}", selector, convertedValue);
        if (convertedValue instanceof Number) {
            return Criteria.where(selector).lte(convertedValue);
        } else {
            // For string comparison, use regex
            return Criteria.where(selector).regex(argument, "i");
        }
    }

    private Criteria buildInCriteria(String selector, List<String> arguments) {
        List<Object> values =
                arguments.stream().map(arg -> convertValue(selector, arg)).collect(Collectors.toList());
        log.debug("Building IN criteria for {}: {}", selector, values);
        return Criteria.where(selector).in(values);
    }

    private Criteria buildNotInCriteria(String selector, List<String> arguments) {
        List<Object> values =
                arguments.stream().map(arg -> convertValue(selector, arg)).collect(Collectors.toList());
        log.debug("Building NOT IN criteria for {}: {}", selector, values);
        return Criteria.where(selector).nin(values);
    }

    /**
     * Converts string argument to appropriate data type based on field selector.
     *
     * This method attempts to convert string arguments to appropriate
     * data types for MongoDB queries based on the field name.
     *
     * @param selector The field name
     * @param argument The string argument to convert
     * @return The converted value
     */
    private Object convertValue(String selector, String argument) {
        if (argument == null || argument.isEmpty()) {
            return argument;
        }

        // Handle null values
        if ("null".equalsIgnoreCase(argument)) {
            return null;
        }

        // Handle boolean values
        if ("isDeleted".equals(selector)) {
            return Boolean.parseBoolean(argument);
        }

        // Handle authorId as string (don't convert to number)
        if ("authorId".equals(selector)) {
            return argument;
        }

        // Handle numeric fields
        if ("likesCount".equals(selector) || "repliesCount".equals(selector)) {
            try {
                return Integer.parseInt(argument);
            } catch (NumberFormatException e) {
                log.warn("Could not convert {} to integer for field {}", argument, selector);
                return argument;
            }
        }

        // Handle date fields
        if ("createdAt".equals(selector) || "updatedAt".equals(selector)) {
            try {
                // Try to parse as ISO date
                return java.time.LocalDateTime.parse(argument);
            } catch (Exception e) {
                log.warn("Could not convert {} to date for field {}", argument, selector);
                return argument;
            }
        }

        // Try to convert to Integer for general numeric fields
        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            // Not an integer, continue
        }

        // Try to convert to Long
        try {
            return Long.parseLong(argument);
        } catch (NumberFormatException e) {
            // Not a long, continue
        }

        // Try to convert to Boolean
        if ("true".equalsIgnoreCase(argument)) {
            return true;
        }
        if ("false".equalsIgnoreCase(argument)) {
            return false;
        }

        // Return as string
        return argument;
    }
}
