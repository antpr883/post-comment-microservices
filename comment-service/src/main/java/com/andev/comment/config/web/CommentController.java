package com.andev.comment.config.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andev.comment.config.model.constants.api.ApiLogMessage;
import com.andev.comment.config.model.domain.CommentDTO;
import com.andev.comment.config.service.CommentService;
import com.andev.comment.config.web.response.CommentsResponse;
import com.andev.comment.config.web.response.PaginationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing comments.
 * Provides endpoints for retrieving comment data with user information.
 */
@RestController
@RequestMapping("${api.comments.path}")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    /**
     * Retrieves a comment by its ID.
     *
     * @param id the comment ID
     * @return the comment with user information
     */
    @GetMapping("{id}")
    public ResponseEntity<CommentsResponse<CommentDTO>> getCommentById(@PathVariable String id) {
        log.info(ApiLogMessage.GET_COMMENT_BY_ID.getMessage(id));
        CommentsResponse<CommentDTO> response = commentService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all comments for a specific post.
     *
     * @param postId the post ID
     * @return list of comments with user information
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<CommentsResponse<PaginationResponse<CommentDTO>>> getCommentsByPostId(
            @PathVariable String postId) {
        log.info("Getting comments for post: {}", postId);
        CommentsResponse<PaginationResponse<CommentDTO>> response = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all comments with their sub-comments (hierarchical structure) for a specific post.
     *
     * @param postId the post ID
     * @return list of comments with nested sub-comments
     */
    @GetMapping("/post/{postId}/hierarchy")
    public ResponseEntity<CommentsResponse<PaginationResponse<CommentDTO>>> getCommentsHierarchy(
            @PathVariable String postId) {
        log.info("Getting comments hierarchy for post: {}", postId);
        CommentsResponse<PaginationResponse<CommentDTO>> response = commentService.getCommentsHierarchy(postId);
        return ResponseEntity.ok(response);
    }
}
