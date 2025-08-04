package com.andev.post.web.endpoints;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.andev.post.model.domain.dto.PostDto;
import com.andev.post.model.domain.dto.request.PostRequestDto;
import com.andev.post.model.domain.dto.request.PostUpdateRequestDto;
import com.andev.post.web.response.AppResponse;
import com.andev.post.web.response.PaginationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Post Management", description = "APIs for managing posts")
public interface PostEndpoints {

    @Operation(summary = "Get post by ID", description = "Retrieves a specific post by its unique identifier")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Post found successfully",
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
                                              "message": "Post retrieved successfully",
                                              "data": {
                                                "id": 1,
                                                "title": "Sample Post",
                                                "content": "This is a sample post content",
                                                "authorId": 123,
                                                "postStatus": "ACTIVE",
                                                "likes": 10,
                                                "created": "2024-01-01T10:00:00",
                                                "updated": "2024-01-01T10:00:00"
                                              }
                                            }
                                            """))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Post not found",
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
                                              "message": "Post not found with id: 1",
                                              "data": null
                                            }
                                            """)))
            })
    @GetMapping("${end.point.id}")
    ResponseEntity<AppResponse<PostDto>> findById(
            @Parameter(description = "Unique identifier of the post", example = "1") @PathVariable Long id);

    @Operation(summary = "Create a new post", description = "Creates a new post with the provided information")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Post created successfully",
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
                                              "message": "Post created successfully",
                                              "data": {
                                                "id": 1,
                                                "title": "New Post",
                                                "content": "This is a new post content",
                                                "authorId": 123,
                                                "postStatus": "ACTIVE",
                                                "likes": 0,
                                                "created": "2024-01-01T10:00:00",
                                                "updated": "2024-01-01T10:00:00"
                                              }
                                            }
                                            """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request data",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = AppResponse.class)))
            })
    @PostMapping
    ResponseEntity<AppResponse<PostDto>> createPost(
            @Parameter(description = "Post creation request", required = true) @RequestBody PostRequestDto requestDto);

    @Operation(
            summary = "Update an existing post",
            description = "Updates an existing post with the provided information")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Post updated successfully"),
                @ApiResponse(responseCode = "404", description = "Post not found"),
                @ApiResponse(responseCode = "400", description = "Invalid request data")
            })
    @PutMapping("${end.point.id}")
    ResponseEntity<AppResponse<PostDto>> updatePost(
            @Parameter(description = "Unique identifier of the post to update", example = "1") @PathVariable Long id,
            @Parameter(description = "Post update request", required = true) @RequestBody
                    PostUpdateRequestDto requestUpdateDto);

    @Operation(summary = "Delete a post", description = "Permanently deletes a post by its ID")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Post deleted successfully"),
                @ApiResponse(responseCode = "404", description = "Post not found")
            })
    @DeleteMapping("${end.point.id}")
    ResponseEntity<AppResponse<PostDto>> deletePost(
            @Parameter(description = "Unique identifier of the post to delete", example = "1") @PathVariable Long id);

    @Operation(
            summary = "Soft delete a post",
            description = "Soft deletes a post by marking it as deleted without removing from database")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Post soft deleted successfully"),
                @ApiResponse(responseCode = "404", description = "Post not found")
            })
    @DeleteMapping("${end.point.id}/soft")
    ResponseEntity<AppResponse<PostDto>> softDeletePost(
            @Parameter(description = "Unique identifier of the post to soft delete", example = "1") @PathVariable
                    Long id);

    @Operation(
            summary = "Get all posts with pagination",
            description =
                    "Retrieves all posts with pagination support. Use sort parameter in format: 'field,direction' (e.g., 'id,asc', 'created,desc')")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Posts retrieved successfully")})
    @GetMapping
    ResponseEntity<AppResponse<PaginationResponse<PostDto>>> getAllPosts(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(
                            description = "Sort field and direction (e.g., 'id,asc', 'created,desc', 'title,asc')",
                            example = "id,asc")
                    @RequestParam(required = false)
                    String sort);

    @Operation(
            summary = "Get posts by author ID",
            description = "Retrieves all posts by a specific author with pagination")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Posts retrieved successfully")})
    @GetMapping("/author/{authorId}")
    ResponseEntity<AppResponse<PaginationResponse<PostDto>>> getPostsByAuthor(
            @Parameter(description = "Author's unique identifier", example = "123") @PathVariable Long authorId,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction (e.g., 'id,asc', 'created,desc')", example = "id,asc")
                    @RequestParam(required = false)
                    String sort);

    @Operation(summary = "Get posts by IDs", description = "Retrieves multiple posts by their IDs with pagination")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Posts retrieved successfully")})
    @GetMapping("/batch")
    ResponseEntity<AppResponse<PaginationResponse<PostDto>>> getPostsByIds(
            @Parameter(description = "List of post IDs", example = "[1, 2, 3]") @RequestParam List<Long> ids,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction (e.g., 'id,asc', 'created,desc')", example = "id,asc")
                    @RequestParam(required = false)
                    String sort);

    @Operation(
            summary = "Get posts by status",
            description = "Retrieves posts filtered by their status with pagination")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Posts retrieved successfully")})
    @GetMapping("/status/{status}")
    ResponseEntity<AppResponse<PaginationResponse<PostDto>>> getPostsByStatus(
            @Parameter(description = "Post status filter", example = "ACTIVE") @PathVariable String status,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction (e.g., 'id,asc', 'created,desc')", example = "id,asc")
                    @RequestParam(required = false)
                    String sort);

    @Operation(summary = "Update post statuses in bulk", description = "Updates the status of multiple posts at once")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Post statuses updated successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid request data")
            })
    @PutMapping("/status/bulk")
    ResponseEntity<Void> updatePostStatuses(
            @Parameter(description = "List of post IDs to update", example = "[1, 2, 3]") @RequestParam List<Long> ids,
            @Parameter(description = "New status for the posts", example = "ACTIVE") @RequestParam String newStatus);
}
