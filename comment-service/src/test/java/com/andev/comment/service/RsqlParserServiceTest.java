package com.andev.comment.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Query;

import com.andev.comment.model.entitie.Comments;
import com.andev.comment.service.rsql.RsqlParserService;

class RsqlParserServiceTest {

    private RsqlParserService<Comments> rsqlParserService;

    @BeforeEach
    void setUp() {
        rsqlParserService = new RsqlParserService<>();
    }

    @Test
    void testSimpleEqualityQuery() {
        String query = "likesCount==5";
        Query result = rsqlParserService.parse(query, Comments.class);

        assertNotNull(result);
        assertNotNull(result.getQueryObject());
        assertEquals(5, result.getQueryObject().get("likesCount"));
    }

    @Test
    void testStringEqualityQuery() {
        String query = "postId==post-001";
        Query result = rsqlParserService.parse(query, Comments.class);

        assertNotNull(result);
        assertNotNull(result.getQueryObject());
        assertEquals("post-001", result.getQueryObject().get("postId"));
    }

    @Test
    void testAndQuery() {
        String query = "likesCount==5;postId==post-001";
        Query result = rsqlParserService.parse(query, Comments.class);

        assertNotNull(result);
        assertNotNull(result.getQueryObject());
        // For AND queries, we expect the query to be processed
        // The actual structure depends on how MongoDB handles AND operations
        assertTrue(result.getQueryObject().size() > 0);
    }

    @Test
    void testEmptyQuery() {
        Query result = rsqlParserService.parse("", Comments.class);
        assertNotNull(result);
    }

    @Test
    void testNullQuery() {
        Query result = rsqlParserService.parse(null, Comments.class);
        assertNotNull(result);
    }

    @Test
    void testInvalidQuery() {
        assertThrows(IllegalArgumentException.class, () -> {
            rsqlParserService.parse("invalid==query==invalid", Comments.class);
        });
    }
}
