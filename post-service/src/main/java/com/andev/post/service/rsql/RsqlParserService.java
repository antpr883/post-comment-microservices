package com.andev.post.service.rsql;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;

@Component
public class RsqlParserService<T> {

    public Specification<T> parse(String query) {
        Node rootNode = new RSQLParser().parse(query);
        return rootNode.accept(new RsqlVisitorImpl<>());
    }
}
