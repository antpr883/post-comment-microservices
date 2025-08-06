package com.andev.post.service.rsql;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import jakarta.persistence.criteria.*;

public class GenericRsqlSpecification<T> implements Specification<T> {

    private final String selector;
    private final ComparisonOperator operator;
    private final List<String> arguments;

    public GenericRsqlSpecification(String selector, ComparisonOperator operator, List<String> arguments) {
        this.selector = selector;
        this.operator = operator;
        this.arguments = arguments;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        if (selector.contains(".")) {
            return JsonSupport.buildJsonPredicate(root, cb, selector, operator, arguments);
        }

        Path<?> path = root.get(selector);
        Object value = JsonSupport.castToRequiredType(path.getJavaType(), arguments.get(0));

        return JsonSupport.buildStandardPredicate(cb, path, value, operator);
    }
}
