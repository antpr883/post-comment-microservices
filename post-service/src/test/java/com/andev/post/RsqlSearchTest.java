package com.andev.post;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

import com.andev.post.model.entities.Post;
import com.andev.post.service.rsql.RsqlParserService;

@SpringBootTest
class RsqlSearchTest {

    @Test
    void testRsqlParserService() {
        // Arrange
        RsqlParserService<Post> rsqlParserService = new RsqlParserService<>();

        // Act & Assert - Test basic equality
        assertDoesNotThrow(() -> {
            Specification<Post> spec = rsqlParserService.parse("title==Test", Post.class);
            assertNotNull(spec);
        });

        // Act & Assert - Test wildcard search
        assertDoesNotThrow(() -> {
            Specification<Post> spec = rsqlParserService.parse("title==*test*", Post.class);
            assertNotNull(spec);
        });

        // Act & Assert - Test JSON field search
        assertDoesNotThrow(() -> {
            Specification<Post> spec = rsqlParserService.parse("description.author==John", Post.class);
            assertNotNull(spec);
        });

        // Act & Assert - Test multiple conditions
        assertDoesNotThrow(() -> {
            Specification<Post> spec = rsqlParserService.parse("postStatus==ACTIVE;likes>10", Post.class);
            assertNotNull(spec);
        });

        // Act & Assert - Test invalid query should throw exception
        assertThrows(Exception.class, () -> {
            rsqlParserService.parse("invalid==query", Post.class);
        });
    }

    @Test
    void testRsqlQueryExamples() {
        RsqlParserService<Post> rsqlParserService = new RsqlParserService<>();

        // Test various RSQL query patterns
        String[] validQueries = {
            "title==Test Post",
            "authorId==123",
            "postStatus==ACTIVE",
            "likes>10",
            "title==*test*",
            "title==Test*",
            "title==*Post",
            "description.author==John Doe",
            "description.category==Technology",
            "postStatus==ACTIVE;likes>10",
            "title==*test*;authorId==123"
        };

        for (String query : validQueries) {
            assertDoesNotThrow(
                    () -> {
                        Specification<Post> spec = rsqlParserService.parse(query, Post.class);
                        assertNotNull(spec, "Query should parse successfully: " + query);
                    },
                    "Query failed to parse: " + query);
        }
    }
}
