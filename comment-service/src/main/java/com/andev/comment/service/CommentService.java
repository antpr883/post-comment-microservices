package com.andev.comment.service;

import com.andev.comment.model.domain.CommentDTO;
import com.andev.comment.web.response.AppResponse;
import com.andev.comment.web.response.PaginationResponse;

/**
 * Service interface for managing comments.
 * Provides operations for retrieving and managing comment data.
 */
public interface CommentService {

    /**
     * Retrieves a comment by its ID.
     *
     * @param id the comment ID
     * @return the comment DTO with user information
     */
    AppResponse<CommentDTO> findById(String id);

    /**
     * Retrieves all comments for a specific post.
     *
     * @param postId the post ID
     * @return list of comment DTOs with user information
     */
    AppResponse<PaginationResponse<CommentDTO>> getCommentsByPostId(String postId);

    /**
     * Retrieves all comments with their sub-comments (hierarchical structure) for a specific post.
     *
     * @param postId the post ID
     * @return list of comments with nested sub-comments
     */
    AppResponse<PaginationResponse<CommentDTO>> getCommentsHierarchy(String postId);

    /**
     * Permanently deletes a comment by its ID.
     *
     * @param id the comment ID to delete
     * @return the deleted comment DTO
     */
    AppResponse<CommentDTO> deleteComment(String id);

    /**
     * Soft deletes a comment by marking it as deleted.
     *
     * @param id the comment ID to soft delete
     * @return the soft-deleted comment DTO
     */
    AppResponse<CommentDTO> softDeleteComment(String id);

    /**
     * Retrieves all comments with pagination and sorting support.
     *
     * @param page the page number (0-based)
     * @param size the number of comments per page
     * @param sort the sorting specification (field,direction)
     * @return paginated list of comments
     */
    AppResponse<PaginationResponse<CommentDTO>> getAllComments(int page, int size, String sort);

    /**
     * Retrieves comments by user ID with pagination and sorting.
     *
     * @param userId the user ID
     * @param page the page number (0-based)
     * @param size the number of comments per page
     * @param sort the sorting specification (field,direction)
     * @return paginated list of comments for the user
     */
    AppResponse<PaginationResponse<CommentDTO>> getCommentsByUserId(String userId, int page, int size, String sort);

    /**
     * Performs advanced search using RSQL (RESTful Service Query Language).
     *
     * @param query the RSQL query string for filtering comments
     * @param page the page number (0-based)
     * @param size the number of comments per page
     * @param sort the sorting specification (field,direction)
     * @return paginated search results
     */
    AppResponse<PaginationResponse<CommentDTO>> searchComments(String query, int page, int size, String sort);
}
