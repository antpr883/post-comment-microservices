package com.andev.comment.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andev.comment.model.domain.CommentDTO;
import com.andev.comment.model.entitie.Comments;
import com.andev.comment.service.CommentService;
import com.andev.comment.service.rsql.SortingHelper;
import com.andev.comment.web.response.CommentsResponse;
import com.andev.comment.web.response.PaginationResponse;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing Comment entities.
 *
 * This controller provides RESTful endpoints for CRUD operations on Comment entities,
 * including advanced search functionality using RSQL (RESTful Service Query Language).
 * It implements the CommentEndpoints interface to ensure consistent API contracts.
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>CRUD operations:</strong> Create, read, update, delete comments</li>
 *   <li><strong>Advanced search:</strong> RSQL-based search with MongoDB support</li>
 *   <li><strong>Pagination:</strong> Support for paginated results</li>
 *   <li><strong>Dynamic sorting:</strong> Automatic field mapping for sorting</li>
 *   <li><strong>Hierarchical comments:</strong> Support for nested comment structures</li>
 *   <li><strong>Soft delete:</strong> Logical deletion with isDeleted flag</li>
 * </ul>
 *
 * <h3>API Endpoints:</h3>
 * <ul>
 *   <li><strong>GET /api/v1/comments/{id}</strong> - Get comment by ID</li>
 *   <li><strong>DELETE /api/v1/comments/{id}</strong> - Hard delete comment</li>
 *   <li><strong>DELETE /api/v1/comments/{id}/soft</strong> - Soft delete comment</li>
 *   <li><strong>GET /api/v1/comments</strong> - Get all comments with pagination</li>
 *   <li><strong>GET /api/v1/comments/user/{userId}</strong> - Get comments by user</li>
 *   <li><strong>GET /api/v1/comments/post/{postId}</strong> - Get comments by post</li>
 *   <li><strong>GET /api/v1/comments/post/{postId}/hierarchy</strong> - Get hierarchical comments</li>
 *   <li><strong>GET /api/v1/comments/search</strong> - Advanced RSQL search</li>
 * </ul>
 *
 * <h3>Search Capabilities:</h3>
 * <ul>
 *   <li><strong>Regular fields:</strong> content==*test*, userId==user456</li>
 *   <li><strong>Numeric fields:</strong> likesCount>5, repliesCount>=2</li>
 *   <li><strong>Logical combinations:</strong> content==*test*;userId==user456</li>
 *   <li><strong>Multiple operators:</strong> ==, !=, >, >=, <, <=, =in=, =out=</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Get all comments with pagination
 * GET /api/v1/comments?page=0&size=10&sort=createdAt,desc
 *
 * // Search comments by content
 * GET /api/v1/comments/search?query=content==*test*
 *
 * // Search comments by user and likes
 * GET /api/v1/comments/search?query=userId==user456;likesCount>5
 *
 * // Get hierarchical comments for a post
 * GET /api/v1/comments/post/post123/hierarchy?page=0&size=10
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
 * @author Comment Service Team
 * @version 1.0
 * @since 2025-08-06
 */
@RestController
@RequestMapping("/${api.comments.path}")
@RequiredArgsConstructor
public class CommentController implements CommentEndpoints {

    /**
     * The comment service for business logic operations.
     * Handles all CRUD operations, search functionality, and business rules.
     */
    private final CommentService commentService;

    @Override
    public ResponseEntity<CommentsResponse<CommentDTO>> findById(String id) {
        CommentsResponse<CommentDTO> response = commentService.findById(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommentsResponse<CommentDTO>> deleteComment(String id) {
        CommentsResponse<CommentDTO> response = commentService.deleteComment(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommentsResponse<CommentDTO>> softDeleteComment(String id) {
        CommentsResponse<CommentDTO> response = commentService.softDeleteComment(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommentsResponse<PaginationResponse<CommentDTO>>> getAllComments(
            int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        CommentsResponse<PaginationResponse<CommentDTO>> response = commentService.getAllComments(page, size, sort);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommentsResponse<PaginationResponse<CommentDTO>>> getCommentsByUserId(
            String userId, int page, int size, String sort) {
        CommentsResponse<PaginationResponse<CommentDTO>> response =
                commentService.getCommentsByUserId(userId, page, size, sort);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommentsResponse<PaginationResponse<CommentDTO>>> getCommentsByPostId(
            String postId, int page, int size, String sort) {
        CommentsResponse<PaginationResponse<CommentDTO>> response = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommentsResponse<PaginationResponse<CommentDTO>>> getCommentsHierarchy(
            String postId, int page, int size, String sort) {
        CommentsResponse<PaginationResponse<CommentDTO>> response = commentService.getCommentsHierarchy(postId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommentsResponse<PaginationResponse<CommentDTO>>> searchComments(
            String query, int page, int size, String sort) {
        CommentsResponse<PaginationResponse<CommentDTO>> response =
                commentService.searchComments(query, page, size, sort);
        return ResponseEntity.ok(response);
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
     *   <li><strong>field,direction</strong> - e.g., "content,asc", "createdAt,desc"</li>
     *   <li><strong>direction values:</strong> "asc" (ascending), "desc" (descending)</li>
     *   <li><strong>default:</strong> If no sort specified, defaults to "id,asc"</li>
     * </ul>
     *
     * <h3>Dynamic Field Mapping:</h3>
     * <ul>
     *   <li><strong>userId</strong> → "userId" (no conversion needed)</li>
     *   <li><strong>postId</strong> → "postId" (no conversion needed)</li>
     *   <li><strong>likesCount</strong> → "likesCount" (no conversion needed)</li>
     *   <li><strong>createdAt</strong> → "createdAt" (no conversion needed)</li>
     * </ul>
     *
     * @param page The page number (0-based)
     * @param size The number of items per page
     * @param sort The sorting specification (field,direction)
     * @return A Pageable object with proper sorting configuration
     *
     * <h3>Examples:</h3>
     * <pre>{@code
     * // Sort by content ascending
     * createPageable(0, 10, "content,asc")
     * // Results in: PageRequest.of(0, 10, Sort.by("content"))
     *
     * // Sort by creation date descending
     * createPageable(0, 10, "createdAt,desc")
     * // Results in: PageRequest.of(0, 10, Sort.by("createdAt", Sort.Direction.DESC))
     *
     * // No sorting specified
     * createPageable(0, 10, null)
     * // Results in: PageRequest.of(0, 10, Sort.by("id", Sort.Direction.ASC))
     * }</pre>
     *
     * <h3>Benefits:</h3>
     * <ul>
     *   <li><strong>No hardcoded mappings:</strong> Automatically adapts to entity changes</li>
     *   <li><strong>Annotation-aware:</strong> Uses @Field annotations for field mapping</li>
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
                String sortField = SortingHelper.getColumnName(Comments.class, field);

                Sort.Direction sortDirection = "DESC".equals(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

                return PageRequest.of(page, size, Sort.by(sortDirection, sortField));
            }
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
    }
}
