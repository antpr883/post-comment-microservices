package com.andev.user.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;

/**
 * RSQL configuration
 */
@Configuration
public class RsqlConfig {

    @Bean
    public RSQLParser rsqlParser() {
        Set<ComparisonOperator> operators = new HashSet<>();
        operators.add(new ComparisonOperator("==", false));
        operators.add(new ComparisonOperator("!=", false));
        operators.add(new ComparisonOperator("=in=", true));
        operators.add(new ComparisonOperator("=out=", true));
        operators.add(new ComparisonOperator("=lt=", false));
        operators.add(new ComparisonOperator("=lte=", false));
        operators.add(new ComparisonOperator("=gt=", false));
        operators.add(new ComparisonOperator("=gte=", false));
        operators.add(new ComparisonOperator("=like=", false));
        operators.add(new ComparisonOperator("=notlike=", false));

        return new RSQLParser(operators);
    }
}
