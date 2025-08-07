package com.andev.post.service.rsql;

import java.util.List;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;

import cz.jirutka.rsql.parser.ast.*;

/**
 * Builder class for creating JPA specifications from RSQL query nodes.
 *
 * This class is responsible for converting RSQL (RESTful Service Query Language)
 * parse tree nodes into Spring Data JPA specifications. It handles both logical
 * operations (AND, OR) and comparison operations, delegating the actual specification
 * creation to GenericRsqlSpecification.
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>RSQL to JPA conversion:</strong> Converts RSQL parse trees to JPA specifications</li>
 *   <li><strong>Logical operations:</strong> Handles AND/OR combinations of conditions</li>
 *   <li><strong>Comparison operations:</strong> Creates specifications for field comparisons</li>
 *   <li><strong>Entity-aware:</strong> Uses entity class for reflection-based field analysis</li>
 *   <li><strong>Type safety:</strong> Ensures proper type handling for specifications</li>
 * </ul>
 *
 * <h3>Processing Flow:</h3>
 * <ol>
 *   <li><strong>Node analysis:</strong> Determine the type of RSQL node</li>
 *   <li><strong>Logical operations:</strong> Handle AND/OR combinations recursively</li>
 *   <li><strong>Comparison operations:</strong> Create specifications for field comparisons</li>
 *   <li><strong>Specification combination:</strong> Combine specifications using JPA criteria</li>
 * </ol>
 *
 * <h3>Supported RSQL Patterns:</h3>
 * <ul>
 *   <li><strong>Simple comparisons:</strong> title==*test*, authorId==123</li>
 *   <li><strong>Logical combinations:</strong> title==*test*;authorId==123 (AND)</li>
 *   <li><strong>Complex queries:</strong> (title==*test*;authorId==123),(status==ACTIVE) (OR)</li>
 *   <li><strong>JSON field queries:</strong> description==*first*, description.summary==*test*</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Create builder for Post entity
 * GenericRsqlSpecBuilder<Post> builder = new GenericRsqlSpecBuilder<>(Post.class);
 *
 * // Parse RSQL query
 * Node node = new RSQLParser().parse("title==*test*;authorId==123");
 *
 * // Create specification
 * Specification<Post> spec = builder.createSpecification(node);
 *
 * // Use in repository
 * List<Post> posts = postRepository.findAll(spec);
 * }</pre>
 *
 * <h3>Why This Approach:</h3>
 * <ul>
 *   <li><strong>Separation of concerns:</strong> Builder handles parsing, Specification handles queries</li>
 *   <li><strong>Reusability:</strong> Can be used with any entity type</li>
 *   <li><strong>Extensibility:</strong> Easy to add new RSQL operators</li>
 *   <li><strong>Performance:</strong> Efficient specification building</li>
 * </ul>
 *
 * @param <T> The entity type for this builder
 * @author Post Service Team
 * @version 1.0
 * @since 2025-08-06
 */
public class GenericRsqlSpecBuilder<T> {

    /**
     * The entity class type for creating specifications.
     * Used to pass entity class information to GenericRsqlSpecification
     * for reflection-based field analysis and JSON detection.
     */
    private final Class<T> entityClass;

    /**
     * Creates a new RSQL specification builder for the given entity type.
     *
     * This constructor initializes the builder with the entity class information
     * needed for creating specifications with proper field analysis.
     *
     * @param entityClass The entity class type (e.g., Post.class)
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * // Create builder for Post entity
     * GenericRsqlSpecBuilder<Post> builder = new GenericRsqlSpecBuilder<>(Post.class);
     *
     * // This enables automatic JSON field detection and proper type casting
     * }</pre>
     */
    public GenericRsqlSpecBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Creates a JPA specification from an RSQL parse tree node.
     *
     * This method is the main entry point for converting RSQL queries to JPA specifications.
     * It analyzes the node type and delegates to appropriate methods for handling
     * logical operations or comparison operations.
     *
     * <h3>Node Types Handled:</h3>
     * <ul>
     *   <li><strong>LogicalNode:</strong> AND/OR combinations (e.g., title==*test*;authorId==123)</li>
     *   <li><strong>ComparisonNode:</strong> Field comparisons (e.g., title==*test*)</li>
     * </ul>
     *
     * <h3>Processing Logic:</h3>
     * <ol>
     *   <li>Check if node is LogicalNode (AND/OR operations)</li>
     *   <li>Check if node is ComparisonNode (field comparisons)</li>
     *   <li>Return null for unsupported node types</li>
     * </ol>
     *
     * @param node The RSQL parse tree node to convert
     * @return A JPA specification that can be used in database queries, or null if node type is unsupported
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * // Parse RSQL query
     * Node node = new RSQLParser().parse("title==*test*;authorId==123");
     *
     * // Create specification
     * Specification<Post> spec = builder.createSpecification(node);
     *
     * // Use in repository
     * List<Post> posts = postRepository.findAll(spec);
     * }</pre>
     */
    public Specification<T> createSpecification(Node node) {
        if (node instanceof LogicalNode logicalNode) {
            return createSpecification(logicalNode);
        } else if (node instanceof ComparisonNode comparisonNode) {
            return createSpecification(comparisonNode);
        }
        return null;
    }

    /**
     * Creates a JPA specification from a logical RSQL node (AND/OR operations).
     *
     * This method handles logical combinations of RSQL conditions. It recursively
     * processes child nodes and combines their specifications using JPA criteria
     * AND/OR operations.
     *
     * <h3>Logical Operations:</h3>
     * <ul>
     *   <li><strong>AND:</strong> All conditions must be true (semicolon-separated in RSQL)</li>
     *   <li><strong>OR:</strong> At least one condition must be true (comma-separated in RSQL)</li>
     * </ul>
     *
     * <h3>Processing Steps:</h3>
     * <ol>
     *   <li>Recursively create specifications for all child nodes</li>
     *   <li>Filter out null specifications</li>
     *   <li>Combine specifications using AND/OR logic</li>
     *   <li>Return always-true predicate if no valid specifications</li>
     * </ol>
     *
     * @param logicalNode The logical RSQL node containing AND/OR operations
     * @return A combined JPA specification for all logical conditions
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * // RSQL: title==*test*;authorId==123 (AND operation)
     * LogicalNode node = parseLogicalNode("title==*test*;authorId==123");
     * Specification<Post> spec = builder.createSpecification(node);
     *
     * // Results in: WHERE title LIKE '%test%' AND author_id = 123
     * }</pre>
     */
    public Specification<T> createSpecification(LogicalNode logicalNode) {
        // Create specifications for all child nodes
        List<Specification<T>> specs = logicalNode.getChildren().stream()
                .map(this::createSpecification)
                .filter(Objects::nonNull)
                .toList(); // Java 17+ or replace with .collect(Collectors.toList()) for Java 8+

        // Return always-true predicate if no valid specifications
        if (specs.isEmpty()) {
            return (root, query, cb) -> cb.conjunction(); // always true
        }

        // Combine specifications using AND/OR logic
        Specification<T> result = specs.get(0);
        for (int i = 1; i < specs.size(); i++) {
            if (logicalNode.getOperator() == LogicalOperator.AND) {
                result = result.and(specs.get(i));
            } else if (logicalNode.getOperator() == LogicalOperator.OR) {
                result = result.or(specs.get(i));
            }
        }

        return result;
    }

    /**
     * Creates a JPA specification from a comparison RSQL node.
     *
     * This method handles individual field comparison operations. It creates
     * a GenericRsqlSpecification that will handle the actual predicate building,
     * including JSON field detection and type casting.
     *
     * <h3>Comparison Operations:</h3>
     * <ul>
     *   <li><strong>Equality:</strong> == (with wildcard support for strings)</li>
     *   <li><strong>Inequality:</strong> !=</li>
     *   <li><strong>Comparison:</strong> >, >=, <, <=</li>
     *   <li><strong>Membership:</strong> =in=, =out=</li>
     * </ul>
     *
     * <h3>Field Types Supported:</h3>
     * <ul>
     *   <li><strong>Regular fields:</strong> title, authorId, postStatus</li>
     *   <li><strong>JSON fields:</strong> description (auto-detected)</li>
     *   <li><strong>JSON paths:</strong> description.summary (explicit paths)</li>
     * </ul>
     *
     * @param node The comparison RSQL node containing field and value information
     * @return A JPA specification for the field comparison
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * // RSQL: title==*test*
     * ComparisonNode node = parseComparisonNode("title==*test*");
     * Specification<Post> spec = builder.createSpecification(node);
     *
     * // Results in: WHERE LOWER(title) LIKE '%test%'
     *
     * // RSQL: description==*first*
     * ComparisonNode node2 = parseComparisonNode("description==*first*");
     * Specification<Post> spec2 = builder.createSpecification(node2);
     *
     * // Results in: WHERE jsonb_extract_path_text(description, 'summary') LIKE '%first%'
     * }</pre>
     */
    public Specification<T> createSpecification(ComparisonNode node) {
        return new GenericRsqlSpecification<>(node.getSelector(), node.getOperator(), node.getArguments(), entityClass);
    }
}
