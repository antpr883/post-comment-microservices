package com.andev.post.service.rsql;

import org.springframework.data.jpa.domain.Specification;

import cz.jirutka.rsql.parser.ast.*;

public class RsqlVisitorImpl<T> implements RSQLVisitor<Specification<T>, Void> {

    private final GenericRsqlSpecBuilder<T> builder;

    public RsqlVisitorImpl(Class<T> entityClass) {
        this.builder = new GenericRsqlSpecBuilder<>(entityClass);
    }

    @Override
    public Specification<T> visit(AndNode node, Void param) {
        return builder.createSpecification(node);
    }

    @Override
    public Specification<T> visit(OrNode node, Void param) {
        return builder.createSpecification(node);
    }

    @Override
    public Specification<T> visit(ComparisonNode node, Void param) {
        return builder.createSpecification(node);
    }
}
