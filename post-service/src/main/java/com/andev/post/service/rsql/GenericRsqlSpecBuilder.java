package com.andev.post.service.rsql;

import java.util.List;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;

import cz.jirutka.rsql.parser.ast.*;

public class GenericRsqlSpecBuilder<T> {

    public Specification<T> createSpecification(Node node) {
        if (node instanceof LogicalNode logicalNode) {
            return createSpecification(logicalNode);
        } else if (node instanceof ComparisonNode comparisonNode) {
            return createSpecification(comparisonNode);
        }
        return null;
    }

    public Specification<T> createSpecification(LogicalNode logicalNode) {
        List<Specification<T>> specs = logicalNode.getChildren().stream()
                .map(this::createSpecification)
                .filter(Objects::nonNull)
                .toList(); // Java 17+ або заміни на .collect(Collectors.toList()) для Java 8+

        if (specs.isEmpty()) {
            return (root, query, cb) -> cb.conjunction(); // always true
        }

        Specification<T> result = specs.get(0);
        for (int i = 1; i < specs.size(); i++) {
            if (logicalNode.getOperator() == LogicalOperator.AND) {
                result = result.and(specs.get(i));
            } else if (logicalNode.getOperator() == LogicalOperator.OR) {
                result = result.or(specs.get(i));
            }
        }

        return result;
    }

    public Specification<T> createSpecification(ComparisonNode node) {
        return new GenericRsqlSpecification<>(node.getSelector(), node.getOperator(), node.getArguments());
    }
}
