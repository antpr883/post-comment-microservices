package com.andev.comment.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.andev.cache.service.UserCacheService;
import com.andev.comment.exception.NotFoundException;
import com.andev.comment.model.domain.CommentDTO;
import com.andev.comment.model.domain.CommentMapper;
import com.andev.comment.model.domain.UserDto;
import com.andev.comment.model.entitie.Comments;
import com.andev.comment.repository.CommentRepository;
import com.andev.comment.service.CommentService;
import com.andev.comment.service.rsql.RsqlParserService;
import com.andev.comment.web.response.CommentsResponse;
import com.andev.comment.web.response.PaginationResponse;

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
    private final MongoTemplate mongoTemplate;
    private final RsqlParserService<Comments> rsqlParserService;

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

    @Override
    public CommentsResponse<CommentDTO> deleteComment(String id) {
        log.info("Deleting comment with id: {}", id);

        Comments comment = commentRepository.findByStringId(id).orElseThrow(() -> {
            log.error("Comment not found with id: {}", id);
            return new NotFoundException("Comment not found with id: " + id);
        });

        commentRepository.delete(comment);
        log.info("Comment deleted successfully: {}", id);

        CommentDTO dto = commentMapper.toDto(comment);
        return CommentsResponse.success(dto);
    }

    @Override
    public CommentsResponse<CommentDTO> softDeleteComment(String id) {
        log.info("Soft deleting comment with id: {}", id);

        Comments comment = commentRepository.findByStringId(id).orElseThrow(() -> {
            log.error("Comment not found with id: {}", id);
            return new NotFoundException("Comment not found with id: " + id);
        });

        comment.setIsDeleted(true);
        Comments savedComment = commentRepository.save(comment);
        log.info("Comment soft deleted successfully: {}", id);

        CommentDTO dto = commentMapper.toDto(savedComment);
        return CommentsResponse.success(dto);
    }

    @Override
    public CommentsResponse<PaginationResponse<CommentDTO>> getAllComments(int page, int size, String sort) {
        log.info("Getting all comments with pagination - page: {}, size: {}, sort: {}", page, size, sort);

        Pageable pageable = createPageable(page, size, sort);
        Page<Comments> commentsPage = commentRepository.findAll(pageable);

        List<CommentDTO> commentDTOs = commentsPage.getContent().stream()
                .map(this::enrichCommentWithUser)
                .toList();

        PaginationResponse<CommentDTO> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(commentDTOs);

        PaginationResponse.Pagination pagination = new PaginationResponse.Pagination();
        pagination.setTotal(commentsPage.getTotalElements());
        pagination.setLimit(size);
        pagination.setCurrentPage(page);
        pagination.setTotalPages(commentsPage.getTotalPages());
        paginationResponse.setPagination(pagination);

        return CommentsResponse.success(paginationResponse);
    }

    @Override
    public CommentsResponse<PaginationResponse<CommentDTO>> getCommentsByUserId(
            String userId, int page, int size, String sort) {
        log.info(
                "Getting comments by userId: {} with pagination - page: {}, size: {}, sort: {}",
                userId,
                page,
                size,
                sort);

        Pageable pageable = createPageable(page, size, sort);
        Query query =
                new Query(Criteria.where("userId").is(userId).and("isDeleted").is(false));
        query.with(pageable);

        List<Comments> comments = mongoTemplate.find(query, Comments.class);
        long total = mongoTemplate.count(query, Comments.class);

        List<CommentDTO> commentDTOs =
                comments.stream().map(this::enrichCommentWithUser).toList();

        PaginationResponse<CommentDTO> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(commentDTOs);

        PaginationResponse.Pagination pagination = new PaginationResponse.Pagination();
        pagination.setTotal(total);
        pagination.setLimit(size);
        pagination.setCurrentPage(page);
        pagination.setTotalPages((int) Math.ceil((double) total / size));
        paginationResponse.setPagination(pagination);

        return CommentsResponse.success(paginationResponse);
    }

    @Override
    public CommentsResponse<PaginationResponse<CommentDTO>> searchComments(
            String query, int page, int size, String sort) {
        log.info("Searching comments with RSQL query: {} - page: {}, size: {}, sort: {}", query, page, size, sort);

        try {
            // Parse RSQL query and create MongoDB query
            Query mongoQuery = rsqlParserService.parse(query, Comments.class);
            mongoQuery.addCriteria(Criteria.where("isDeleted").is(false));

            // Count total before pagination
            long total = mongoTemplate.count(mongoQuery, Comments.class);

            // Apply pagination
            Pageable pageable = createPageable(page, size, sort);
            mongoQuery.with(pageable);

            List<Comments> comments = mongoTemplate.find(mongoQuery, Comments.class);

            List<CommentDTO> commentDTOs =
                    comments.stream().map(this::enrichCommentWithUser).toList();

            PaginationResponse<CommentDTO> paginationResponse = new PaginationResponse<>();
            paginationResponse.setContent(commentDTOs);

            PaginationResponse.Pagination pagination = new PaginationResponse.Pagination();
            pagination.setTotal(total);
            pagination.setLimit(size);
            pagination.setCurrentPage(page);
            pagination.setTotalPages((int) Math.ceil((double) total / size));
            paginationResponse.setPagination(pagination);

            return CommentsResponse.success(paginationResponse);

        } catch (Exception e) {
            log.error("Error parsing RSQL query: {}", query, e);
            throw new IllegalArgumentException("Invalid RSQL query: " + e.getMessage());
        }
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

    private CommentDTO enrichCommentWithUser(Comments comment) {
        CommentDTO dto = commentMapper.toDto(comment);

        if (comment.getUserId() != null) {
            try {
                Optional<Map<String, Object>> userData =
                        userCacheService.getUserById(Long.valueOf(comment.getUserId()));

                if (userData.isPresent()) {
                    UserDto userDto = new UserDto();
                    userDto.setUserId((Long) userData.get().get("userId"));
                    userDto.setUsername((String) userData.get().get("username"));
                    dto.setUser(userDto);
                }
            } catch (Exception e) {
                log.error("Error fetching user for comment {}: {}", comment.getId(), e.getMessage());
            }
        }

        return dto;
    }

    private Pageable createPageable(int page, int size, String sort) {
        if (sort != null && !sort.trim().isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String field = sortParts[0].trim();
                String direction = sortParts[1].trim().toUpperCase();

                Sort.Direction sortDirection = "DESC".equals(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
                return PageRequest.of(page, size, Sort.by(sortDirection, field));
            }
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
    }

    // Removed convertSpecificationToQuery method as it's no longer needed
}
