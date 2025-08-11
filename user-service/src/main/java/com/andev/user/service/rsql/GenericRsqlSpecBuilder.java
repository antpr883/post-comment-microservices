package com.andev.user.service.rsql;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import cz.jirutka.rsql.parser.ast.*;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic RSQL specification builder
 */
@Slf4j
public class GenericRsqlSpecBuilder<T> {

    public Specification<T> createSpecification(Node node) {
        if (node instanceof LogicalNode) {
            return createSpecification((LogicalNode) node);
        }
        if (node instanceof ComparisonNode) {
            return createSpecification((ComparisonNode) node);
        }
        return null;
    }

    public Specification<T> createSpecification(LogicalNode logicalNode) {
        List<Specification<T>> specs = logicalNode.getChildren().stream()
                .map(node -> createSpecification(node))
                .toList();

        Specification<T> result = specs.get(0);
        if (logicalNode.getOperator() == LogicalOperator.AND) {
            for (int i = 1; i < specs.size(); i++) {
                result = Specification.where(result).and(specs.get(i));
            }
        } else if (logicalNode.getOperator() == LogicalOperator.OR) {
            for (int i = 1; i < specs.size(); i++) {
                result = Specification.where(result).or(specs.get(i));
            }
        }

        return result;
    }

    public Specification<T> createSpecification(ComparisonNode comparisonNode) {
        return (root, query, criteriaBuilder) -> {
            List<String> arguments = comparisonNode.getArguments();
            String selector = comparisonNode.getSelector();
            ComparisonOperator operator = comparisonNode.getOperator();

            Path<String> path = root.get(selector);

            switch (operator.getSymbol()) {
                case "==":
                    return criteriaBuilder.equal(path, arguments.get(0));
                case "!=":
                    return criteriaBuilder.notEqual(path, arguments.get(0));
                case "=in=":
                    return path.in(arguments);
                case "=out=":
                    return criteriaBuilder.not(path.in(arguments));
                case "=lt=":
                    return criteriaBuilder.lessThan(path, arguments.get(0));
                case "=lte=":
                    return criteriaBuilder.lessThanOrEqualTo(path, arguments.get(0));
                case "=gt=":
                    return criteriaBuilder.greaterThan(path, arguments.get(0));
                case "=gte=":
                    return criteriaBuilder.greaterThanOrEqualTo(path, arguments.get(0));
                case "=like=":
                    return criteriaBuilder.like(path, "%" + arguments.get(0) + "%");
                case "=notlike=":
                    return criteriaBuilder.notLike(path, "%" + arguments.get(0) + "%");
                default:
                    throw new IllegalArgumentException("Unknown operator: " + operator);
            }
        };
    }
}
