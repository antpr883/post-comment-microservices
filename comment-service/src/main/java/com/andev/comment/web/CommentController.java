package com.andev.comment.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andev.comment.model.domain.CommentDTO;
import com.andev.comment.service.CommentService;
import com.andev.comment.web.response.AppResponse;
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
    public ResponseEntity<AppResponse<CommentDTO>> findById(String id) {
        AppResponse<CommentDTO> response = commentService.findById(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AppResponse<CommentDTO>> deleteComment(String id) {
        AppResponse<CommentDTO> response = commentService.deleteComment(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AppResponse<CommentDTO>> softDeleteComment(String id) {
        AppResponse<CommentDTO> response = commentService.softDeleteComment(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AppResponse<PaginationResponse<CommentDTO>>> getAllComments(int page, int size, String sort) {
        AppResponse<PaginationResponse<CommentDTO>> response = commentService.getAllComments(page, size, sort);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AppResponse<PaginationResponse<CommentDTO>>> getCommentsByUserId(
            String userId, int page, int size, String sort) {
        AppResponse<PaginationResponse<CommentDTO>> response =
                commentService.getCommentsByUserId(userId, page, size, sort);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AppResponse<PaginationResponse<CommentDTO>>> getCommentsByPostId(
            String postId, int page, int size, String sort) {
        AppResponse<PaginationResponse<CommentDTO>> response = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AppResponse<PaginationResponse<CommentDTO>>> getCommentsHierarchy(
            String postId, int page, int size, String sort) {
        AppResponse<PaginationResponse<CommentDTO>> response = commentService.getCommentsHierarchy(postId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AppResponse<PaginationResponse<CommentDTO>>> searchComments(
            String query, int page, int size, String sort) {
        AppResponse<PaginationResponse<CommentDTO>> response = commentService.searchComments(query, page, size, sort);
        return ResponseEntity.ok(response);
    }
}
