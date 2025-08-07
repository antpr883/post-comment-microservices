package com.andev.post.service.rsql;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import jakarta.persistence.criteria.*;

public class GenericRsqlSpecification<T> implements Specification<T> {

    private final String selector;
    private final ComparisonOperator operator;
    private final List<String> arguments;
    private final Class<T> entityClass;

    public GenericRsqlSpecification(
            String selector, ComparisonOperator operator, List<String> arguments, Class<T> entityClass) {
        this.selector = selector;
        this.operator = operator;
        this.arguments = arguments;
        this.entityClass = entityClass;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        // Strategy 1: Check for explicit JSON path (contains dot notation)
        if (selector.contains(".")) {
            return JsonSupport.buildJsonPredicate(root, cb, selector, operator, arguments);
        }

        // Strategy 2: Check if field is JSON type using reflection
        if (JsonFieldUtils.isJsonField(entityClass, selector)) {
            // Automatically search in JSON fields (e.g., description.summary)
            return JsonSupport.buildAutoJsonPredicate(root, cb, selector, operator, arguments);
        }

        // Strategy 3: Standard field comparison (strings, numbers, dates, etc.)
        Path<?> path = root.get(selector);
        Object value = JsonSupport.castToRequiredType(path.getJavaType(), arguments.get(0));

        return JsonSupport.buildStandardPredicate(cb, path, value, operator);
    }
}
