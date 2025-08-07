package com.andev.post.web.endpoints;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andev.post.model.domain.dto.PostDto;
import com.andev.post.model.domain.dto.request.PostRequestDto;
import com.andev.post.model.domain.dto.request.PostUpdateRequestDto;
import com.andev.post.model.entities.Post;
import com.andev.post.service.PostService;
import com.andev.post.service.rsql.SortingHelper;
import com.andev.post.web.response.AppResponse;
import com.andev.post.web.response.PaginationResponse;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing Post entities.
 *
 * This controller provides RESTful endpoints for CRUD operations on Post entities,
 * including advanced search functionality using RSQL (RESTful Service Query Language).
 * It implements the PostEndpoints interface to ensure consistent API contracts.
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>CRUD operations:</strong> Create, read, update, delete posts</li>
 *   <li><strong>Advanced search:</strong> RSQL-based search with JSON field support</li>
 *   <li><strong>Pagination:</strong> Support for paginated results</li>
 *   <li><strong>Dynamic sorting:</strong> Automatic field mapping for sorting</li>
 *   <li><strong>Bulk operations:</strong> Batch status updates</li>
 *   <li><strong>Soft delete:</strong> Logical deletion with status updates</li>
 * </ul>
 *
 * <h3>API Endpoints:</h3>
 * <ul>
 *   <li><strong>GET /api/v1/posts/{id}</strong> - Get post by ID</li>
 *   <li><strong>POST /api/v1/posts</strong> - Create new post</li>
 *   <li><strong>PUT /api/v1/posts/{id}</strong> - Update existing post</li>
 *   <li><strong>DELETE /api/v1/posts/{id}</strong> - Hard delete post</li>
 *   <li><strong>DELETE /api/v1/posts/{id}/soft</strong> - Soft delete post</li>
 *   <li><strong>GET /api/v1/posts</strong> - Get all posts with pagination</li>
 *   <li><strong>GET /api/v1/posts/search</strong> - Advanced RSQL search</li>
 *   <li><strong>PUT /api/v1/posts/bulk/status</strong> - Bulk status update</li>
 * </ul>
 *
 * <h3>Search Capabilities:</h3>
 * <ul>
 *   <li><strong>Regular fields:</strong> title==*test*, authorId==123</li>
 *   <li><strong>JSON fields:</strong> description==*first* (auto-detected)</li>
 *   <li><strong>JSON paths:</strong> description.summary==*test* (explicit)</li>
 *   <li><strong>Logical combinations:</strong> title==*test*;authorId==123</li>
 *   <li><strong>Multiple operators:</strong> ==, !=, >, >=, <, <=, =in=, =out=</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Get all posts with pagination
 * GET /api/v1/posts?page=0&size=10&sort=title,asc
 *
 * // Search posts by title
 * GET /api/v1/posts/search?query=title==*test*
 *
 * // Search in JSON fields
 * GET /api/v1/posts/search?query=description==*first*
 *
 * // Complex search with multiple conditions
 * GET /api/v1/posts/search?query=title==*test*;authorId==123;description==*first*
 *
 * // Create new post
 * POST /api/v1/posts
 * {
 *   "title": "New Post",
 *   "content": "Post content",
 *   "authorId": 123
 * }
 * }</pre>
 *
 * <h3>Response Format:</h3>
 * <pre>{@code
 * {
 *   "success": true,
 *   "message": "Resource founded",
 *   "payload": {
 *     "content": [...],
 *     "pagination": {
 *       "total": 100,
 *       "limit": 10,
 *       "page": 0,
 *       "pages": 10
 *     }
 *   }
 * }
 * }</pre>
 *
 * @author Post Service Team
 * @version 1.0
 * @since 2025-08-06
 */
@RestController
@RequestMapping("${end.point.posts}")
@RequiredArgsConstructor
public class PostController implements PostEndpoints {

    /**
     * The post service for business logic operations.
     * Handles all CRUD operations, search functionality, and business rules.
     */
    private final PostService postService;

    /**
     * Retrieves a post by its unique identifier.
     *
     * This endpoint fetches a single post by its ID and returns it with
     * enriched user data if available. The response includes the post
     * details along with author information.
     *
     * @param id The unique identifier of the post to retrieve
     * @return ResponseEntity containing the post data or error response
     *
     * <h3>Response Examples:</h3>
     * <pre>{@code
     * // Success response
     * {
     *   "success": true,
     *   "message": "Resource founded",
     *   "payload": {
     *     "id": 1,
     *     "title": "First Post",
     *     "content": "Post content",
     *     "authorId": 123,
     *     "userDto": {
     *       "userId": 123,
     *       "username": "john_doe"
     *     }
     *   }
     * }
     *
     * // Error response (post not found)
     * {
     *   "success": false,
     *   "message": "Resource not found with id: 999",
     *   "payload": null
     * }
     * }</pre>
     */
    @Override
    public ResponseEntity<AppResponse<PostDto>> findById(Long id) {
        AppResponse<PostDto> byId = postService.findById(id);
        return ResponseEntity.ok(byId);
    }

    /**
     * Creates a new post.
     *
     * This endpoint creates a new post with the provided data. The post
     * is automatically assigned an ACTIVE status and enriched with
     * user information if available.
     *
     * @param requestDto The post creation request containing title, content, and authorId
     * @return ResponseEntity containing the created post data
     *
     * <h3>Request Example:</h3>
     * <pre>{@code
     * {
     *   "title": "New Post Title",
     *   "content": "This is the content of the new post",
     *   "authorId": 123
     * }
     * }</pre>
     *
     * <h3>Validation Rules:</h3>
     * <ul>
     *   <li>Title: 5-255 characters, required</li>
     *   <li>Content: minimum 10 characters, required</li>
     *   <li>AuthorId: required, must be valid user ID</li>
     * </ul>
     */
    @Override
    public ResponseEntity<AppResponse<PostDto>> createPost(PostRequestDto requestDto) {
        AppResponse<PostDto> createdPost = postService.create(requestDto);
        return ResponseEntity.ok(createdPost);
    }

    /**
     * Updates an existing post.
     *
     * This endpoint updates a post with the provided data. Only the fields
     * that are present in the request will be updated (partial updates supported).
     *
     * @param id The unique identifier of the post to update
     * @param requestUpdateDto The post update request containing fields to update
     * @return ResponseEntity containing the updated post data
     *
     * <h3>Request Example:</h3>
     * <pre>{@code
     * {
     *   "title": "Updated Post Title",
     *   "content": "Updated content"
     * }
     * }</pre>
     *
     * <h3>Partial Update Support:</h3>
     * <ul>
     *   <li>Only provided fields are updated</li>
     *   <li>Null fields are ignored</li>
     *   <li>Validation applies only to provided fields</li>
     * </ul>
     */
    @Override
    public ResponseEntity<AppResponse<PostDto>> updatePost(Long id, PostUpdateRequestDto requestUpdateDto) {
        AppResponse<PostDto> updatedPost = postService.update(id, requestUpdateDto);
        return ResponseEntity.ok(updatedPost);
    }

    /**
     * Permanently deletes a post.
     *
     * This endpoint performs a hard delete of the post, removing it
     * completely from the database. This action cannot be undone.
     *
     * @param id The unique identifier of the post to delete
     * @return ResponseEntity containing the deleted post data
     *
     * <h3>Warning:</h3>
     * <ul>
     *   <li>This operation is irreversible</li>
     *   <li>All post data is permanently removed</li>
     *   <li>Consider using soft delete for data preservation</li>
     * </ul>
     */
    @Override
    public ResponseEntity<AppResponse<PostDto>> deletePost(Long id) {
        AppResponse<PostDto> deletedPost = postService.delete(id);
        return ResponseEntity.ok(deletedPost);
    }

    /**
     * Soft deletes a post by updating its status.
     *
     * This endpoint performs a soft delete by setting the post status
     * to DELETED. The post remains in the database but is marked as deleted.
     *
     * @param id The unique identifier of the post to soft delete
     * @return ResponseEntity containing the soft-deleted post data
     *
     * <h3>Benefits of Soft Delete:</h3>
     * <ul>
     *   <li>Data preservation for audit purposes</li>
     *   <li>Ability to restore deleted posts</li>
     *   <li>Maintains referential integrity</li>
     * </ul>
     */
    @Override
    public ResponseEntity<AppResponse<PostDto>> softDeletePost(Long id) {
        AppResponse<PostDto> softDeletedPost = postService.softDelete(id);
        return ResponseEntity.ok(softDeletedPost);
    }

    /**
     * Retrieves all posts with pagination and sorting support.
     *
     * This endpoint returns a paginated list of all posts with optional
     * sorting. The response includes pagination metadata and enriched
     * user data for each post.
     *
     * @param page The page number (0-based)
     * @param size The number of posts per page
     * @param sort The sorting specification (field,direction)
     * @return ResponseEntity containing paginated post data
     *
     * <h3>Sorting Examples:</h3>
     * <ul>
     *   <li><strong>title,asc</strong> - Sort by title ascending</li>
     *   <li><strong>authorId,desc</strong> - Sort by author ID descending</li>
     *   <li><strong>created,desc</strong> - Sort by creation date descending</li>
     * </ul>
     *
     * <h3>Response Format:</h3>
     * <pre>{@code
     * {
     *   "success": true,
     *   "message": "Resource founded",
     *   "payload": {
     *     "content": [
     *       {
     *         "id": 1,
     *         "title": "First Post",
     *         "content": "Content",
     *         "authorId": 123,
     *         "userDto": {...}
     *       }
     *     ],
     *     "pagination": {
     *       "total": 100,
     *       "limit": 10,
     *       "page": 0,
     *       "pages": 10
     *     }
     *   }
     * }
     * }</pre>
     */
    @Override
    public ResponseEntity<AppResponse<PaginationResponse<PostDto>>> getAllPosts(int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        AppResponse<PaginationResponse<PostDto>> posts = postService.findAll(pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Retrieves posts by author ID with pagination and sorting.
     *
     * This endpoint returns all posts written by a specific author,
     * with pagination and sorting support.
     *
     * @param authorId The unique identifier of the author
     * @param page The page number (0-based)
     * @param size The number of posts per page
     * @param sort The sorting specification (field,direction)
     * @return ResponseEntity containing paginated post data for the author
     */
    @Override
    public ResponseEntity<AppResponse<PaginationResponse<PostDto>>> getPostsByAuthor(
            Long authorId, int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        AppResponse<PaginationResponse<PostDto>> posts = postService.findByAuthorId(authorId, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Retrieves posts by multiple IDs with pagination and sorting.
     *
     * This endpoint returns posts that match any of the provided IDs,
     * with pagination and sorting support.
     *
     * @param ids List of post IDs to retrieve
     * @param page The page number (0-based)
     * @param size The number of posts per page
     * @param sort The sorting specification (field,direction)
     * @return ResponseEntity containing paginated post data
     */
    @Override
    public ResponseEntity<AppResponse<PaginationResponse<PostDto>>> getPostsByIds(
            List<Long> ids, int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        AppResponse<PaginationResponse<PostDto>> posts = postService.findByIds(ids, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Retrieves posts by status with pagination and sorting.
     *
     * This endpoint returns all posts with a specific status,
     * with pagination and sorting support.
     *
     * @param status The post status to filter by (ACTIVE, INACTIVE, DELETED)
     * @param page The page number (0-based)
     * @param size The number of posts per page
     * @param sort The sorting specification (field,direction)
     * @return ResponseEntity containing paginated post data
     */
    @Override
    public ResponseEntity<AppResponse<PaginationResponse<PostDto>>> getPostsByStatus(
            String status, int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        AppResponse<PaginationResponse<PostDto>> posts = postService.findByStatus(status, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Creates a Pageable object with proper sorting handling using dynamic field mapping.
     *
     * This method creates a Spring Data Pageable object with sorting support.
     * It uses the SortingHelper to dynamically map entity field names to
     * database column names, eliminating the need for hardcoded field mappings.
     *
     * <h3>Sorting Format:</h3>
     * <ul>
     *   <li><strong>field,direction</strong> - e.g., "title,asc", "authorId,desc"</li>
     *   <li><strong>direction values:</strong> "asc" (ascending), "desc" (descending)</li>
     *   <li><strong>default:</strong> If no sort specified, defaults to "id,asc"</li>
     * </ul>
     *
     * <h3>Dynamic Field Mapping:</h3>
     * <ul>
     *   <li><strong>authorId</strong> → "author_id" (from @Column annotation)</li>
     *   <li><strong>postStatus</strong> → "post_status" (from @Column annotation)</li>
     *   <li><strong>title</strong> → "title" (no conversion needed)</li>
     *   <li><strong>createdAt</strong> → "created_at" (camelCase to snake_case)</li>
     * </ul>
     *
     * @param page The page number (0-based)
     * @param size The number of items per page
     * @param sort The sorting specification (field,direction)
     * @return A Pageable object with proper sorting configuration
     *
     * <h3>Examples:</h3>
     * <pre>{@code
     * // Sort by title ascending
     * createPageable(0, 10, "title,asc")
     * // Results in: PageRequest.of(0, 10, Sort.by("title"))
     *
     * // Sort by author ID descending
     * createPageable(0, 10, "authorId,desc")
     * // Results in: PageRequest.of(0, 10, Sort.by("author_id", Sort.Direction.DESC))
     *
     * // No sorting specified
     * createPageable(0, 10, null)
     * // Results in: PageRequest.of(0, 10, Sort.by("id", Sort.Direction.ASC))
     * }</pre>
     *
     * <h3>Benefits:</h3>
     * <ul>
     *   <li><strong>No hardcoded mappings:</strong> Automatically adapts to entity changes</li>
     *   <li><strong>Annotation-aware:</strong> Uses @Column annotations for field mapping</li>
     *   <li><strong>Fallback support:</strong> Converts camelCase to snake_case when needed</li>
     *   <li><strong>Error handling:</strong> Graceful fallback to original field name</li>
     * </ul>
     */
    private Pageable createPageable(int page, int size, String sort) {
        if (sort != null && !sort.trim().isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String field = sortParts[0].trim();
                String direction = sortParts[1].trim().toUpperCase();

                // Use dynamic field mapping instead of hardcoded values
                String sortField = SortingHelper.getColumnName(Post.class, field);

                Sort.Direction sortDirection = "DESC".equals(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

                return PageRequest.of(page, size, Sort.by(sortDirection, sortField));
            }
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
    }

    /**
     * Updates the status of multiple posts in a single operation.
     *
     * This endpoint performs a bulk update operation to change the status
     * of multiple posts simultaneously. This is useful for batch operations
     * like bulk activation, deactivation, or deletion.
     *
     * @param ids List of post IDs to update
     * @param newStatus The new status to assign to all specified posts
     * @return ResponseEntity with no content (204 No Content)
     *
     * <h3>Supported Status Values:</h3>
     * <ul>
     *   <li><strong>ACTIVE</strong> - Post is visible and active</li>
     *   <li><strong>INACTIVE</strong> - Post is hidden but not deleted</li>
     *   <li><strong>DELETED</strong> - Post is soft deleted</li>
     * </ul>
     *
     * <h3>Request Example:</h3>
     * <pre>{@code
     * PUT /api/v1/posts/bulk/status
     * {
     *   "ids": [1, 2, 3, 4, 5],
     *   "newStatus": "ACTIVE"
     * }
     * }</pre>
     *
     * <h3>Benefits:</h3>
     * <ul>
     *   <li><strong>Performance:</strong> Single database operation for multiple updates</li>
     *   <li><strong>Atomicity:</strong> All updates succeed or fail together</li>
     *   <li><strong>Efficiency:</strong> Reduces database round trips</li>
     * </ul>
     */
    @Override
    public ResponseEntity<Void> updatePostStatuses(List<Long> ids, String newStatus) {
        postService.updateStatuses(ids, newStatus);
        return ResponseEntity.ok().build();
    }

    /**
     * Performs advanced search using RSQL (RESTful Service Query Language).
     *
     * This endpoint provides powerful search capabilities using RSQL syntax.
     * It supports searching in regular fields, JSON fields, and complex
     * logical combinations of conditions.
     *
     * <h3>RSQL Query Examples:</h3>
     * <ul>
     *   <li><strong>Simple search:</strong> title==*test*</li>
     *   <li><strong>JSON field search:</strong> description==*first*</li>
     *   <li><strong>JSON path search:</strong> description.summary==*test*</li>
     *   <li><strong>Multiple conditions:</strong> title==*test*;authorId==123</li>
     *   <li><strong>Complex queries:</strong> (title==*test*;authorId==123),(status==ACTIVE)</li>
     * </ul>
     *
     * <h3>Supported Operators:</h3>
     * <ul>
     *   <li><strong>==</strong> - Equal (with wildcard support for strings)</li>
     *   <li><strong>!=</strong> - Not equal</li>
     *   <li><strong>>, >=, <, <=</strong> - Comparison operators</li>
     *   <li><strong>=in=</strong> - IN operator (multiple values)</li>
     *   <li><strong>=out=</strong> - NOT IN operator</li>
     * </ul>
     *
     * <h3>Wildcard Support:</h3>
     * <ul>
     *   <li><strong>*value*</strong> - Contains value (case-insensitive)</li>
     *   <li><strong>*value</strong> - Ends with value</li>
     *   <li><strong>value*</strong> - Starts with value</li>
     * </ul>
     *
     * @param query The RSQL query string for filtering posts
     * @param page The page number (0-based)
     * @param size The number of posts per page
     * @param sort The sorting specification (field,direction)
     * @return ResponseEntity containing paginated search results
     *
     * <h3>Request Examples:</h3>
     * <pre>{@code
     * // Search posts with "test" in title
     * GET /api/v1/posts/search?query=title==*test*
     *
     * // Search in JSON description field
     * GET /api/v1/posts/search?query=description==*first*
     *
     * // Complex search with multiple conditions
     * GET /api/v1/posts/search?query=title==*test*;authorId==123;description==*first*
     *
     * // Search with pagination and sorting
     * GET /api/v1/posts/search?query=title==*test*&page=0&size=10&sort=title,asc
     * }</pre>
     *
     * <h3>Generated SQL Examples:</h3>
     * <pre>{@code
     * -- Query: title==*test*
     * SELECT * FROM posts WHERE LOWER(title) LIKE '%test%'
     *
     * -- Query: description==*first*
     * SELECT * FROM posts WHERE jsonb_extract_path_text(description, 'summary') LIKE '%first%'
     *
     * -- Query: title==*test*;authorId==123
     * SELECT * FROM posts WHERE LOWER(title) LIKE '%test%' AND author_id = 123
     * }</pre>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *   <li><strong>Invalid syntax:</strong> Returns 400 Bad Request with error details</li>
     *   <li><strong>Invalid fields:</strong> Returns 400 Bad Request for non-existent fields</li>
     *   <li><strong>Type mismatches:</strong> Returns 400 Bad Request for incompatible types</li>
     * </ul>
     */
    @Override
    public ResponseEntity<AppResponse<PaginationResponse<PostDto>>> searchPosts(
            String query, int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        AppResponse<PaginationResponse<PostDto>> posts = postService.search(query, pageable);
        return ResponseEntity.ok(posts);
    }
}
