package com.andev.post.service.rsql;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;

/**
 * Service class for parsing RSQL queries and converting them to JPA specifications.
 *
 * This class provides a high-level interface for converting RSQL (RESTful Service Query Language)
 * query strings into Spring Data JPA specifications. It acts as the main entry point
 * for the RSQL parsing system and coordinates the parsing and conversion process.
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>RSQL parsing:</strong> Converts RSQL query strings to parse trees</li>
 *   <li><strong>Specification creation:</strong> Converts parse trees to JPA specifications</li>
 *   <li><strong>Entity-aware:</strong> Uses entity class for proper field analysis</li>
 *   <li><strong>Error handling:</strong> Provides meaningful error messages for invalid queries</li>
 *   <li><strong>Spring integration:</strong> Managed as a Spring component</li>
 * </ul>
 *
 * <h3>Processing Flow:</h3>
 * <ol>
 *   <li><strong>Query parsing:</strong> RSQLParser converts string to parse tree</li>
 *   <li><strong>Tree traversal:</strong> RsqlVisitorImpl traverses the parse tree</li>
 *   <li><strong>Specification building:</strong> GenericRsqlSpecBuilder creates specifications</li>
 *   <li><strong>Result return:</strong> Returns JPA specification for database queries</li>
 * </ol>
 *
 * <h3>Supported Query Patterns:</h3>
 * <ul>
 *   <li><strong>Simple queries:</strong> title==*test*, authorId==123</li>
 *   <li><strong>Logical combinations:</strong> title==*test*;authorId==123 (AND)</li>
 *   <li><strong>Complex queries:</strong> (title==*test*;authorId==123),(status==ACTIVE) (OR)</li>
 *   <li><strong>JSON field queries:</strong> description==*first*, description.summary==*test*</li>
 *   <li><strong>Multiple operators:</strong> ==, !=, >, >=, <, <=, =in=, =out=</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Inject the service
 * @Autowired
 * private RsqlParserService<Post> rsqlParserService;
 *
 * // Parse RSQL query
 * String rsqlQuery = "title==*test*;authorId==123;description==*first*";
 * Specification<Post> spec = rsqlParserService.parse(rsqlQuery, Post.class);
 *
 * // Use in repository
 * List<Post> posts = postRepository.findAll(spec);
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
 * @author Post Service Team
 * @version 1.0
 * @since 2025-08-06
 */
@Component
public class RsqlParserService<T> {

    /**
     * Parses an RSQL query string and converts it to a JPA specification.
     *
     * This method is the main entry point for RSQL query processing. It takes
     * an RSQL query string and converts it into a Spring Data JPA specification
     * that can be used for database queries.
     *
     * <h3>Processing Steps:</h3>
     * <ol>
     *   <li><strong>Parse query:</strong> Convert RSQL string to parse tree</li>
     *   <li><strong>Create visitor:</strong> Initialize visitor with entity class</li>
     *   <li><strong>Traverse tree:</strong> Use visitor to convert tree to specification</li>
     *   <li><strong>Return result:</strong> Return JPA specification</li>
     * </ol>
     *
     * <h3>Query Examples:</h3>
     * <ul>
     *   <li><strong>Simple:</strong> "title==*test*"</li>
     *   <li><strong>AND combination:</strong> "title==*test*;authorId==123"</li>
     *   <li><strong>OR combination:</strong> "title==*test*,status==ACTIVE"</li>
     *   <li><strong>Complex:</strong> "(title==*test*;authorId==123),(status==ACTIVE)"</li>
     *   <li><strong>JSON field:</strong> "description==*first*"</li>
     *   <li><strong>JSON path:</strong> "description.summary==*test*"</li>
     * </ul>
     *
     * <h3>Generated SQL Examples:</h3>
     * <pre>{@code
     * -- Query: "title==*test*;authorId==123"
     * SELECT * FROM posts
     * WHERE LOWER(title) LIKE '%test%' AND author_id = 123
     *
     * -- Query: "description==*first*"
     * SELECT * FROM posts
     * WHERE jsonb_extract_path_text(description, 'summary') LIKE '%first%'
     *
     * -- Query: "title==*test*,status==ACTIVE"
     * SELECT * FROM posts
     * WHERE LOWER(title) LIKE '%test%' OR post_status = 'ACTIVE'
     * }</pre>
     *
     * @param query The RSQL query string to parse
     * @param entityClass The entity class for proper field analysis and JSON detection
     * @return A JPA specification that can be used in database queries
     *
     * @throws cz.jirutka.rsql.parser.ParseException if the RSQL query has invalid syntax
     * @throws IllegalArgumentException if the query references non-existent fields or invalid types
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * // Parse complex RSQL query
     * String query = "title==*test*;authorId==123;description==*first*";
     * Specification<Post> spec = rsqlParserService.parse(query, Post.class);
     *
     * // Use in repository with pagination
     * Page<Post> page = postRepository.findAll(spec, PageRequest.of(0, 10));
     *
     * // Use in repository with sorting
     * List<Post> posts = postRepository.findAll(spec, Sort.by("title"));
     * }</pre>
     *
     * <h3>Error Handling:</h3>
     * <pre>{@code
     * try {
     *     Specification<Post> spec = rsqlParserService.parse(query, Post.class);
     *     // Use specification
     * } catch (ParseException e) {
     *     // Handle invalid RSQL syntax
     *     log.error("Invalid RSQL query: {}", query, e);
     * } catch (IllegalArgumentException e) {
     *     // Handle invalid fields or types
     *     log.error("Invalid query parameters: {}", query, e);
     * }
     * }</pre>
     */
    public Specification<T> parse(String query, Class<T> entityClass) {
        // Parse RSQL query string to parse tree
        Node rootNode = new RSQLParser().parse(query);

        // Convert parse tree to JPA specification using visitor pattern
        return rootNode.accept(new RsqlVisitorImpl<>(entityClass));
    }
}
