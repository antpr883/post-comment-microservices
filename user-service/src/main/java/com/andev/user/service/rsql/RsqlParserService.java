package com.andev.user.service.rsql;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for parsing RSQL queries into JPA Specifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RsqlParserService {

    private final RSQLParser rsqlParser;

    /**
     * Parse RSQL query string into JPA Specification
     */
    public <T> Specification<T> parse(String rsqlQuery) {
        if (rsqlQuery == null || rsqlQuery.trim().isEmpty()) {
            return null;
        }

        try {
            log.debug("Parsing RSQL query: {}", rsqlQuery);
            Node rootNode = rsqlParser.parse(rsqlQuery);
            RsqlVisitorImpl<T> visitor = new RsqlVisitorImpl<>();
            return rootNode.accept(visitor);
        } catch (Exception e) {
            log.error("Error parsing RSQL query: {}", rsqlQuery, e);
            throw new IllegalArgumentException("Invalid RSQL query: " + rsqlQuery, e);
        }
    }
}
