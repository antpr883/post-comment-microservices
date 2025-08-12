package com.andev.comment.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.andev.comment.model.domain.CommentDTO;
import com.andev.comment.web.response.AppResponse;
import com.andev.comment.web.response.PaginationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST API endpoints interface for managing comments.
 *
 * This interface defines all REST endpoints for comment management operations,
 * including CRUD operations, search functionality, and hierarchical comment retrieval.
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>CRUD operations:</strong> Create, read, update, delete comments</li>
 *   <li><strong>Advanced search:</strong> RSQL-based search with MongoDB support</li>
 *   <li><strong>Pagination:</strong> Support for paginated results</li>
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
 * @author Comment Service Team
 * @version 1.0
 * @since 2025-08-06
 */
@Tag(name = "Comment Management", description = "APIs for managing comments")
public interface CommentEndpoints {

    @Operation(summary = "Get comment by ID", description = "Retrieves a specific comment by its unique identifier")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Comment found successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = AppResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Success Response",
                                                        value =
                                                                """
                                            {
                                              "success": true,
                                              "message": "Comment retrieved successfully",
                                              "data": {
                                                "id": "507f1f77bcf86cd799439011",
                                                "postId": "post123",
                                                "content": "This is a great post!",
                                                "userId": "user456",
                                                "parentCommentId": null,
                                                "likesCount": 5,
                                                "repliesCount": 2,
                                                "isDeleted": false,
                                                "createdAt": "2024-01-01T10:00:00",
                                                "updatedAt": "2024-01-01T10:00:00",
                                                "user": {
                                                  "userId": 456,
                                                  "username": "john_doe"
                                                }
                                              }
                                            }
                                            """))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Comment not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = AppResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Not Found Response",
                                                        value =
                                                                """
                                            {
                                              "success": false,
                                              "message": "Comment not found with id: 507f1f77bcf86cd799439011",
                                              "data": null
                                            }
                                            """)))
            })
    @GetMapping("/{id}")
    ResponseEntity<AppResponse<CommentDTO>> findById(
            @Parameter(description = "Unique identifier of the comment", example = "507f1f77bcf86cd799439011")
                    @PathVariable
                    String id);

    @Operation(summary = "Delete a comment", description = "Permanently deletes a comment by its ID")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
                @ApiResponse(responseCode = "404", description = "Comment not found")
            })
    @DeleteMapping("/{id}")
    ResponseEntity<AppResponse<CommentDTO>> deleteComment(
            @Parameter(description = "Unique identifier of the comment to delete", example = "507f1f77bcf86cd799439011")
                    @PathVariable
                    String id);

    @Operation(
            summary = "Soft delete a comment",
            description = "Soft deletes a comment by marking it as deleted without removing from database")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Comment soft deleted successfully"),
                @ApiResponse(responseCode = "404", description = "Comment not found")
            })
    @DeleteMapping("/{id}/soft")
    ResponseEntity<AppResponse<CommentDTO>> softDeleteComment(
            @Parameter(
                            description = "Unique identifier of the comment to soft delete",
                            example = "507f1f77bcf86cd799439011")
                    @PathVariable
                    String id);

    @Operation(
            summary = "Get all comments with pagination",
            description =
                    "Retrieves all comments with pagination support. Use sort parameter in format: 'field,direction' (e.g., 'id,asc', 'createdAt,desc')")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Comments retrieved successfully")})
    @GetMapping
    ResponseEntity<AppResponse<PaginationResponse<CommentDTO>>> getAllComments(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(
                            description = "Sort field and direction (e.g., 'id,asc', 'createdAt,desc', 'content,asc')",
                            example = "id,asc")
                    @RequestParam(required = false)
                    String sort);

    @Operation(
            summary = "Get comments by user ID",
            description = "Retrieves all comments by a specific user with pagination")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Comments retrieved successfully")})
    @GetMapping("/user/{userId}")
    ResponseEntity<AppResponse<PaginationResponse<CommentDTO>>> getCommentsByUserId(
            @Parameter(description = "User's unique identifier", example = "user456") @PathVariable String userId,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction (e.g., 'id,asc', 'createdAt,desc')", example = "id,asc")
                    @RequestParam(required = false)
                    String sort);

    @Operation(
            summary = "Get comments by post ID",
            description = "Retrieves all comments for a specific post with pagination")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Comments retrieved successfully")})
    @GetMapping("/post/{postId}")
    ResponseEntity<AppResponse<PaginationResponse<CommentDTO>>> getCommentsByPostId(
            @Parameter(description = "Post's unique identifier", example = "post123") @PathVariable String postId,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction (e.g., 'id,asc', 'createdAt,desc')", example = "id,asc")
                    @RequestParam(required = false)
                    String sort);

    @Operation(
            summary = "Get comments hierarchy by post ID",
            description = "Retrieves all comments with their sub-comments (hierarchical structure) for a specific post")
    @ApiResponses(
            value = {@ApiResponse(responseCode = "200", description = "Comments hierarchy retrieved successfully")})
    @GetMapping("/post/{postId}/hierarchy")
    ResponseEntity<AppResponse<PaginationResponse<CommentDTO>>> getCommentsHierarchy(
            @Parameter(description = "Post's unique identifier", example = "post123") @PathVariable String postId,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction (e.g., 'id,asc', 'createdAt,desc')", example = "id,asc")
                    @RequestParam(required = false)
                    String sort);

    @Operation(
            summary = "Search comments using RSQL query",
            description =
                    "Search comments using RSQL (RESTful Service Query Language) with support for complex filtering. "
                            + "Examples: "
                            + "'content==*test*' (content contains 'test'), "
                            + "'userId==user456' (exact user ID), "
                            + "'postId==post123' (exact post ID), "
                            + "'likesCount>5' (likes greater than 5), "
                            + "'content==*test*;userId==user456;likesCount>5' (complex query)")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Comments found successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = AppResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Success Response",
                                                        value =
                                                                """
                                            {
                                              "success": true,
                                              "message": "Comments found successfully",
                                              "data": {
                                                "content": [
                                                  {
                                                    "id": "507f1f77bcf86cd799439011",
                                                    "postId": "post123",
                                                    "content": "Test comment",
                                                    "userId": "user456",
                                                    "parentCommentId": null,
                                                    "likesCount": 10,
                                                    "repliesCount": 2,
                                                    "isDeleted": false,
                                                    "createdAt": "2024-01-01T10:00:00",
                                                    "updatedAt": "2024-01-01T10:00:00",
                                                    "user": {
                                                      "userId": 456,
                                                      "username": "john_doe"
                                                    }
                                                  }
                                                ],
                                                "pagination": {
                                                  "total": 1,
                                                  "limit": 10,
                                                  "page": 0,
                                                  "pages": 1
                                                }
                                              }
                                            }
                                            """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid RSQL query",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = AppResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Invalid Query Response",
                                                        value =
                                                                """
                                            {
                                              "success": false,
                                              "message": "Invalid RSQL query: Unsupported operator",
                                              "data": null
                                            }
                                            """)))
            })
    @GetMapping("/search")
    ResponseEntity<AppResponse<PaginationResponse<CommentDTO>>> searchComments(
            @Parameter(
                            description = "RSQL query string for filtering comments. "
                                    + "Supported operators: ==, !=, >, >=, <, <=. "
                                    + "Multiple conditions separated by semicolon: content==*test*;userId==user456",
                            example = "content==*test*;userId==user456;likesCount>5")
                    @RequestParam
                    String query,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(
                            description = "Sort field and direction (e.g., 'id,asc', 'createdAt,desc', 'content,asc')",
                            example = "id,asc")
                    @RequestParam(required = false)
                    String sort);
}
