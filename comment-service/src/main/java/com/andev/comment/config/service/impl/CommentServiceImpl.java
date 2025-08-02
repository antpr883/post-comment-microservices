package com.andev.comment.config.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.andev.cache.service.UserCacheService;
import com.andev.comment.config.exception.NotFoundException;
import com.andev.comment.config.model.domain.CommentDTO;
import com.andev.comment.config.model.domain.CommentMapper;
import com.andev.comment.config.model.domain.UserDto;
import com.andev.comment.config.model.entitie.Comments;
import com.andev.comment.config.repository.CommentRepository;
import com.andev.comment.config.service.CommentService;
import com.andev.comment.config.web.response.CommentsResponse;
import com.andev.comment.config.web.response.PaginationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of CommentService that provides comment management functionality.
 * Includes user information fetching using UserCacheService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Autowired(required = false)
    private UserCacheService userCacheService;

    @Override
    public CommentsResponse<CommentDTO> findById(String id) {
        log.info("Searching for comment with id: {}", id);

        Comments comment = commentRepository.findByStringId(id).orElseThrow(() -> {
            log.error("Comment not found with id: {}", id);
            return new NotFoundException("Comment not found with id: " + id);
        });

        log.info("Found comment: {}", comment);
        CommentDTO dto = commentMapper.toDto(comment);

        // Fetch and set user information if available
        if (comment.getUserId() != null) {
            try {
                Optional<Map<String, Object>> userData =
                        userCacheService.getUserById(Long.valueOf(comment.getUserId()));

                if (userData.isPresent()) {
                    // Convert Map to local UserDto
                    UserDto commentServiceUserDto = new UserDto();
                    commentServiceUserDto.setUserId((Long) userData.get().get("userId"));
                    commentServiceUserDto.setUsername((String) userData.get().get("username"));

                    dto.setUser(commentServiceUserDto);
                    log.info(
                            "Found user for comment {}: {}",
                            comment.getId(),
                            userData.get().get("username"));
                } else {
                    log.warn("User not found for comment {} with userId: {}", comment.getId(), comment.getUserId());
                }
            } catch (Exception e) {
                log.error("Error fetching user for comment {}: {}", comment.getId(), e.getMessage());
            }
        }

        return CommentsResponse.success(dto);
    }

    @Override
    public CommentsResponse<PaginationResponse<CommentDTO>> getCommentsByPostId(String postId) {
        log.info("Searching for comments with postId: {}", postId);

        List<Comments> comments = commentRepository.findAllByPostId(postId);
        log.info("Found {} comments for postId: {}", comments.size(), postId);

        List<CommentDTO> commentDTOs = comments.stream()
                .map(comment -> {
                    CommentDTO dto = commentMapper.toDto(comment);

                    // Fetch and set user information if available
                    if (comment.getUserId() != null) {
                        try {
                            Optional<Map<String, Object>> userData =
                                    userCacheService.getUserById(Long.valueOf(comment.getUserId()));

                            if (userData.isPresent()) {
                                // Convert Map to local UserDto
                                UserDto commentServiceUserDto = new UserDto();
                                commentServiceUserDto.setUserId(
                                        (Long) userData.get().get("userId"));
                                commentServiceUserDto.setUsername(
                                        (String) userData.get().get("username"));

                                dto.setUser(commentServiceUserDto);
                                log.debug(
                                        "Found user for comment {}: {}",
                                        comment.getId(),
                                        userData.get().get("username"));
                            } else {
                                log.warn(
                                        "User not found for comment {} with userId: {}",
                                        comment.getId(),
                                        comment.getUserId());
                            }
                        } catch (Exception e) {
                            log.error("Error fetching user for comment {}: {}", comment.getId(), e.getMessage());
                        }
                    }

                    return dto;
                })
                .toList();

        PaginationResponse<CommentDTO> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(commentDTOs);

        PaginationResponse.Pagination pagination = new PaginationResponse.Pagination();
        pagination.setTotal(commentDTOs.size());
        pagination.setLimit(commentDTOs.size());
        pagination.setCurrentPage(0);
        pagination.setTotalPages(1);
        paginationResponse.setPagination(pagination);

        return CommentsResponse.success(paginationResponse);
    }

    @Override
    public CommentsResponse<PaginationResponse<CommentDTO>> getCommentsHierarchy(String postId) {
        log.info("Building comments hierarchy for postId: {}", postId);

        // Get all comments for the post
        List<Comments> allComments = commentRepository.findAllByPostId(postId);
        log.info("Found {} comments for postId: {}", allComments.size(), postId);

        // Separate main comments (parentCommentId = null) and sub-comments
        List<Comments> mainComments = allComments.stream()
                .filter(comment -> comment.getParentCommentId() == null)
                .toList();

        List<Comments> subComments = allComments.stream()
                .filter(comment -> comment.getParentCommentId() != null)
                .toList();

        // Build hierarchy
        List<CommentDTO> hierarchyComments = mainComments.stream()
                .map(mainComment -> {
                    CommentDTO mainDto = commentMapper.toDto(mainComment);

                    // Find sub-comments for this main comment
                    List<CommentDTO> replies = getReplies(subComments, mainComment.getId());

                    mainDto.setReplies(replies);

                    // Fetch user info for main comment
                    if (mainComment.getUserId() != null) {
                        try {
                            Optional<Map<String, Object>> userData =
                                    userCacheService.getUserById(Long.valueOf(mainComment.getUserId()));

                            if (userData.isPresent()) {
                                UserDto userDto = new UserDto();
                                userDto.setUserId((Long) userData.get().get("userId"));
                                userDto.setUsername((String) userData.get().get("username"));
                                mainDto.setUser(userDto);
                            }
                        } catch (Exception e) {
                            log.error("Error fetching user for comment {}: {}", mainComment.getId(), e.getMessage());
                        }
                    }

                    return mainDto;
                })
                .toList();

        PaginationResponse<CommentDTO> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(hierarchyComments);

        PaginationResponse.Pagination pagination = new PaginationResponse.Pagination();
        pagination.setTotal(hierarchyComments.size());
        pagination.setLimit(hierarchyComments.size());
        pagination.setCurrentPage(0);
        pagination.setTotalPages(1);
        paginationResponse.setPagination(pagination);

        return CommentsResponse.success(paginationResponse);
    }

    private List<CommentDTO> getReplies(List<Comments> subComments, String parentCommentId) {
        return subComments.stream()
                .filter(sub -> parentCommentId.equals(sub.getParentCommentId()))
                .map(subComment -> {
                    CommentDTO subDto = commentMapper.toDto(subComment);

                    // Fetch user info for sub-comment
                    if (subComment.getUserId() != null) {
                        try {
                            Optional<Map<String, Object>> userData =
                                    userCacheService.getUserById(Long.valueOf(subComment.getUserId()));

                            if (userData.isPresent()) {
                                UserDto userDto = new UserDto();
                                userDto.setUserId((Long) userData.get().get("userId"));
                                userDto.setUsername((String) userData.get().get("username"));
                                subDto.setUser(userDto);
                            }
                        } catch (Exception e) {
                            log.error("Error fetching user for comment {}: {}", subComment.getId(), e.getMessage());
                        }
                    }

                    return subDto;
                })
                .toList();
    }
}
