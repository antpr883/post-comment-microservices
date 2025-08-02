package com.andev.comment.service;

import com.andev.comment.model.domain.CommentDTO;
import com.andev.comment.web.response.CommentsResponse;
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
    CommentsResponse<CommentDTO> findById(String id);

    /**
     * Retrieves all comments for a specific post.
     *
     * @param postId the post ID
     * @return list of comment DTOs with user information
     */
    CommentsResponse<PaginationResponse<CommentDTO>> getCommentsByPostId(String postId);

    /**
     * Retrieves all comments with their sub-comments (hierarchical structure) for a specific post.
     *
     * @param postId the post ID
     * @return list of comments with nested sub-comments
     */
    CommentsResponse<PaginationResponse<CommentDTO>> getCommentsHierarchy(String postId);
}
