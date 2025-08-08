package com.andev.comment.service.rsql;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;

/**
 * Service class for parsing RSQL queries and converting them to MongoDB queries.
 *
 * This class provides a high-level interface for converting RSQL (RESTful Service Query Language)
 * query strings into MongoDB queries. It acts as the main entry point for the RSQL parsing system
 * and coordinates the parsing and conversion process.
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>RSQL parsing:</strong> Converts RSQL query strings to parse trees</li>
 *   <li><strong>MongoDB query creation:</strong> Converts parse trees to MongoDB queries</li>
 *   <li><strong>Entity-aware:</strong> Uses entity class for proper field analysis</li>
 *   <li><strong>Error handling:</strong> Provides meaningful error messages for invalid queries</li>
 *   <li><strong>Spring integration:</strong> Managed as a Spring component</li>
 * </ul>
 *
 * <h3>Processing Flow:</h3>
 * <ol>
 *   <li><strong>Query parsing:</strong> RSQLParser converts string to parse tree</li>
 *   <li><strong>Tree traversal:</strong> RsqlVisitorImpl traverses the parse tree</li>
 *   <li><strong>Query building:</strong> Creates MongoDB Criteria objects</li>
 *   <li><strong>Result return:</strong> Returns MongoDB Query for database operations</li>
 * </ol>
 *
 * <h3>Supported Query Patterns:</h3>
 * <ul>
 *   <li><strong>Simple queries:</strong> content==*test*, userId==user456</li>
 *   <li><strong>Logical combinations:</strong> content==*test*;userId==user456 (AND)</li>
 *   <li><strong>Complex queries:</strong> (content==*test*;userId==user456),(likesCount>5) (OR)</li>
 *   <li><strong>Multiple operators:</strong> ==, !=, >, >=, <, <=, =in=, =out=</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Inject the service
 * @Autowired
 * private RsqlParserService<Comments> rsqlParserService;
 *
 * // Parse RSQL query
 * String rsqlQuery = "content==*test*;userId==user456;likesCount>5";
 * Query query = rsqlParserService.parse(rsqlQuery, Comments.class);
 *
 * // Use in MongoDB operations
 * List<Comments> comments = mongoTemplate.find(query, Comments.class);
 * }</pre>
 *
 * <h3>Error Handling:</h3>
 * <ul>
 *   <li><strong>Invalid syntax:</strong> Throws ParseException for malformed queries</li>
 *   <li><strong>Invalid fields:</strong> Throws IllegalArgumentException for non-existent fields</li>
 *   <li><strong>Type mismatches:</strong> Throws IllegalArgumentException for incompatible types</li>
 * </ul>
 *
 * <h3>Why This Service:</h3>
 * <ul>
 *   <li><strong>Centralized parsing:</strong> Single point of entry for RSQL processing</li>
 *   <li><strong>Spring integration:</strong> Easy to inject and use in other components</li>
 *   <li><strong>Type safety:</strong> Generic type ensures proper entity handling</li>
 *   <li><strong>Error handling:</strong> Centralized error handling and logging</li>
 * </ul>
 *
 * @param <T> The entity type for this parser service
 * @author Comment Service Team
 * @version 1.0
 * @since 2025-08-06
 */
@Component
public class RsqlParserService<T> {

    private static final Logger log = LoggerFactory.getLogger(RsqlParserService.class);

    /**
     * Parses an RSQL query string and converts it to a MongoDB query.
     *
     * This method is the main entry point for RSQL query processing. It takes
     * an RSQL query string and converts it into a MongoDB query that can be
     * used for database operations.
     *
     * <h3>Processing Steps:</h3>
     * <ol>
     *   <li><strong>Parse query:</strong> Convert RSQL string to parse tree</li>
     *   <li><strong>Create visitor:</strong> Initialize visitor with entity class</li>
     *   <li><strong>Traverse tree:</strong> Use visitor to convert tree to query</li>
     *   <li><strong>Return result:</strong> Return MongoDB query</li>
     * </ol>
     *
     * <h3>Query Examples:</h3>
     * <ul>
     *   <li><strong>Simple:</strong> "content==*test*"</li>
     *   <li><strong>AND combination:</strong> "content==*test*;userId==user456"</li>
     *   <li><strong>OR combination:</strong> "content==*test*,likesCount>5"</li>
     *   <li><strong>Complex:</strong> "(content==*test*;userId==user456),(likesCount>5)"</li>
     * </ul>
     *
     * <h3>Generated MongoDB Query Examples:</h3>
     * <pre>{@code
     * -- Query: "content==*test*;userId==user456"
     * db.comments.find({
     *   "content": { "$regex": "test", "$options": "i" },
     *   "userId": "user456"
     * })
     *
     * -- Query: "likesCount>5"
     * db.comments.find({
     *   "likesCount": { "$gt": 5 }
     * })
     *
     * -- Query: "content==*test*,likesCount>5"
     * db.comments.find({
     *   "$or": [
     *     { "content": { "$regex": "test", "$options": "i" } },
     *     { "likesCount": { "$gt": 5 } }
     *   ]
     * })
     * }</pre>
     *
     * @param query The RSQL query string to parse
     * @param entityClass The entity class for proper field analysis
     * @return A MongoDB query that can be used in database operations
     *
     * @throws cz.jirutka.rsql.parser.ParseException if the RSQL query has invalid syntax
     * @throws IllegalArgumentException if the query references non-existent fields or invalid types
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * // Parse complex RSQL query
     * String query = "content==*test*;userId==user456;likesCount>5";
     * Query mongoQuery = rsqlParserService.parse(query, Comments.class);
     *
     * // Use in MongoDB operations with pagination
     * mongoQuery.with(PageRequest.of(0, 10));
     * List<Comments> comments = mongoTemplate.find(mongoQuery, Comments.class);
     *
     * // Use in MongoDB operations with sorting
     * mongoQuery.with(Sort.by("createdAt").descending());
     * List<Comments> comments = mongoTemplate.find(mongoQuery, Comments.class);
     * }</pre>
     *
     * <h3>Error Handling:</h3>
     * <pre>{@code
     * try {
     *     Query mongoQuery = rsqlParserService.parse(query, Comments.class);
     *     // Use query
     * } catch (ParseException e) {
     *     // Handle invalid RSQL syntax
     *     log.error("Invalid RSQL query: {}", query, e);
     * } catch (IllegalArgumentException e) {
     *     // Handle invalid fields or types
     *     log.error("Invalid query parameters: {}", query, e);
     * }
     * }</pre>
     */
    public Query parse(String query, Class<T> entityClass) {
        if (query == null || query.trim().isEmpty()) {
            log.warn("Empty or null RSQL query provided");
            return new Query();
        }

        try {
            log.debug("Parsing RSQL query: {}", query);

            // Handle URL encoding issues
            String decodedQuery = URLDecoder.decode(query, "UTF-8");
            log.debug("Decoded RSQL query: {}", decodedQuery);

            // Parse RSQL query string to parse tree
            Node rootNode = new RSQLParser().parse(decodedQuery);

            // Convert parse tree to MongoDB query using visitor pattern
            Query result = rootNode.accept(new RsqlVisitorImpl<T>(entityClass));

            log.debug("Generated MongoDB query: {}", result.getQueryObject());
            return result;

        } catch (UnsupportedEncodingException e) {
            log.error("Failed to decode RSQL query: {}", query, e);
            throw new IllegalArgumentException("Invalid URL encoding in RSQL query: " + query, e);
        } catch (Exception e) {
            log.error("Failed to parse RSQL query: {}", query, e);
            throw new IllegalArgumentException("Invalid RSQL query syntax: " + query + ". Error: " + e.getMessage(), e);
        }
    }
}
