package com.andev.comment.config.envloader;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

/**
 * MongoDB configuration for the comment service.
 * Enables MongoDB auditing and repository scanning.
 */
@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(List.of(new StringIdConverter()));
    }

    public static class StringIdConverter implements Converter<String, String> {
        @Override
        public String convert(String source) {
            return source;
        }
    }
}
