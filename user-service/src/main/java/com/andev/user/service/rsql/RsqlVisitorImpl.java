package com.andev.user.service.rsql;

import org.springframework.data.jpa.domain.Specification;

import cz.jirutka.rsql.parser.ast.*;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;

/**
 * RSQL visitor implementation for JPA Specifications
 */
@Slf4j
public class RsqlVisitorImpl<T> implements RSQLVisitor<Specification<T>, Root<T>> {

    private final GenericRsqlSpecBuilder<T> builder;

    public RsqlVisitorImpl() {
        this.builder = new GenericRsqlSpecBuilder<>();
    }

    @Override
    public Specification<T> visit(AndNode node, Root<T> root) {
        log.debug("Visiting AND node: {}", node);
        return builder.createSpecification(node);
    }

    @Override
    public Specification<T> visit(OrNode node, Root<T> root) {
        log.debug("Visiting OR node: {}", node);
        return builder.createSpecification(node);
    }

    @Override
    public Specification<T> visit(ComparisonNode node, Root<T> root) {
        log.debug("Visiting comparison node: {}", node);
        return builder.createSpecification(node);
    }
}
