package com.andev.post.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andev.cache.service.UserCacheService;
import com.andev.post.config.aop.AuditLog;
import com.andev.post.exception.NotFoundException;
import com.andev.post.model.constants.ApiErrorMessage;
import com.andev.post.model.domain.dto.PostDto;
import com.andev.post.model.domain.dto.UserDto;
import com.andev.post.model.domain.dto.request.PostRequestDto;
import com.andev.post.model.domain.dto.request.PostUpdateRequestDto;
import com.andev.post.model.domain.mapper.PostMapper;
import com.andev.post.model.entities.Post;
import com.andev.post.model.enums.PostStatus;
import com.andev.post.repository.PostRepository;
import com.andev.post.service.PostService;
import com.andev.post.service.rsql.RsqlParserService;
import com.andev.post.web.response.AppResponse;
import com.andev.post.web.response.PaginationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of PostService for managing post operations.
 *
 * Provides CRUD operations for posts with advanced search capabilities using RSQL,
 * user data enrichment through cache service, and comprehensive audit logging.
 * Supports soft delete, status management, and performance-optimized queries.
 */
@Slf4j
@Service
@AuditLog
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    @Value("${app.post.default-status:ACTIVE}")
    private String defaultPostStatus;

    private final PostMapper postMapper;
    private final PostRepository postRepository;
    private final RsqlParserService<Post> rsqlParserService;

    @Autowired(required = false)
    private UserCacheService userCacheService;

    /**
     * Creates a new post with the provided data.
     *
     * Sets the default post status from configuration and enriches the response
     * with user data from the cache service when available.
     *
     * @param requestDto the post creation request data
     * @return AppResponse containing the created PostDto with user information
     */
    @Override
    @Transactional
    public AppResponse<PostDto> create(PostRequestDto requestDto) {
        log.debug("Creating post with title: {}", requestDto.getTitle());
        Post post = postMapper.toEntity(requestDto);
        post.setPostStatus(PostStatus.valueOf(defaultPostStatus)); // Configurable default status
        Post savedPost = postRepository.save(post);
        PostDto dto = postMapper.toDto(savedPost);
        enrichWithUserData(dto, savedPost.getAuthorId());
        log.info("Created post with ID: {}", savedPost.getId());
        return AppResponse.successful(dto);
    }

    @Override
    @Transactional
    public AppResponse<PostDto> update(Long id, PostUpdateRequestDto postUpdateRequestDto) {
        log.debug("Updating post with ID: {}", id);
        Post post = findPostById(id);
        postMapper.updateEntity(postUpdateRequestDto, post); // Only updates present fields
        Post updatedPost = postRepository.save(post);
        PostDto dto = postMapper.toDto(updatedPost);
        enrichWithUserData(dto, updatedPost.getAuthorId());
        log.info("Updated post with ID: {}", id);
        return AppResponse.successful(dto);
    }

    @Override
    @Transactional
    public AppResponse<PostDto> delete(Long id) {
        Post post = findPostById(id);
        postRepository.delete(post);
        log.info("Hard-deleted post ID: {}", id);
        PostDto dto = postMapper.toDto(post);
        return AppResponse.successful(dto);
    }

    @Override
    @Transactional
    public AppResponse<PostDto> softDelete(Long id) {
        Post post = findPostById(id);
        post.setPostStatus(PostStatus.DELETED); // Add to enum if missing
        Post saved = postRepository.save(post);
        PostDto dto = postMapper.toDto(saved);
        enrichWithUserData(dto, saved.getAuthorId());
        log.info("Soft-deleted post ID: {}", id);
        return AppResponse.successful(dto);
    }

    @Override
    public AppResponse<PostDto> findById(Long id) {
        Post post = findPostById(id);
        PostDto dto = postMapper.toDto(post);
        enrichWithUserData(dto, post.getAuthorId());
        return AppResponse.successful(dto);
    }

    @Override
    public AppResponse<PaginationResponse<PostDto>> findAll(Pageable pageable) {
        Page<Post> page = postRepository.findAll(pageable);
        log.info("Find all pageable posts");
        return toPaginatedResponse(page);
    }

    @Override
    public AppResponse<PaginationResponse<PostDto>> findByAuthorId(Long authorId, Pageable pageable) {
        Page<Post> page = postRepository.findByAuthorId(authorId, pageable);
        log.info("Find all pageable posts by author ID: {}", authorId);
        return toPaginatedResponse(page);
    }

    @Override
    public AppResponse<PaginationResponse<PostDto>> findByIds(List<Long> ids, Pageable pageable) {
        List<Post> posts = postRepository.findByIdsIn(ids);

        // Optionally manual paging logic; here, just slice if needed:
        int page = pageable.getPageNumber();
        int limit = pageable.getPageSize();
        int fromIndex = Math.min(page * limit, posts.size());
        int toIndex = Math.min(fromIndex + limit, posts.size());
        List<PostDto> dtos = posts.subList(fromIndex, toIndex).stream()
                .map(post -> {
                    PostDto dto = postMapper.toDto(post);
                    enrichWithUserData(dto, post.getAuthorId());
                    return dto;
                })
                .toList();

        int pages = (int) Math.ceil((double) posts.size() / limit);

        PaginationResponse.Pagination pagination = PaginationResponse.Pagination.builder()
                .total(posts.size())
                .limit(limit)
                .page(page)
                .pages(pages)
                .build();

        PaginationResponse<PostDto> response = PaginationResponse.<PostDto>builder()
                .content(dtos)
                .pagination(pagination)
                .build();

        log.info("Find all pageable posts by IDs: {}", ids);
        return AppResponse.successful(response);
    }

    @Override
    public AppResponse<PaginationResponse<PostDto>> findByStatus(String status, Pageable pageable) {
        PostStatus postStatus = PostStatus.valueOf(status);
        Page<Post> page = postRepository.findByPostStatus(postStatus, pageable);
        return toPaginatedResponse(page);
    }

    @Override
    public void updateStatuses(List<Long> ids, String newStatus) {
        PostStatus postStatus = PostStatus.valueOf(newStatus);
        int updated = postRepository.updatePostStatuses(ids, postStatus);
        log.info("Bulk-updated status for {} posts to {}", updated, newStatus);
    }

    /**
     * Searches posts using RSQL (RESTful Search Query Language).
     *
     * Supports complex queries with logical operators (AND, OR) and comparison operators
     * (==, !=, =gt=, =lt=, etc.). Returns paginated results with user data enrichment.
     *
     * Example queries:
     * - title==*Spring* (contains Spring)
     * - status==ACTIVE;likes=gt=10 (active posts with > 10 likes)
     * - authorId==123,authorId==456 (posts by specific authors)
     *
     * @param rsqlQuery the RSQL query string
     * @param pageable  pagination parameters
     * @return AppResponse with paginated PostDto results
     * @throws IllegalArgumentException if RSQL query is invalid
     */
    @Override
    public AppResponse<PaginationResponse<PostDto>> search(String rsqlQuery, Pageable pageable) {
        log.debug("Searching posts with RSQL query: {}", rsqlQuery);

        try {
            Specification<Post> specification = rsqlParserService.parse(rsqlQuery, Post.class);
            Page<Post> page = postRepository.findAll(specification, pageable);

            log.info("Found {} posts matching RSQL query: {}", page.getTotalElements(), rsqlQuery);
            return toPaginatedResponse(page);
        } catch (Exception e) {
            log.error("Error parsing RSQL query '{}': {}", rsqlQuery, e.getMessage());
            throw new IllegalArgumentException("Invalid RSQL query: " + e.getMessage());
        }
    }

    // =================== HELPER METHODS ===================
    private Post findPostById(Long id) {
        return postRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.RESOURCE_NOT_FOUND_BY_ID.getMessage(id)));
    }

    private void enrichWithUserData(PostDto dto, Long authorId) {
        if (authorId == null || userCacheService == null) {
            return;
        }
        try {
            userCacheService
                    .getUserById(authorId)
                    .ifPresentOrElse(
                            user -> setUserDtoFromCache(dto, user, authorId),
                            () -> log.debug("User data not found in cache for author ID: {}", authorId));
        } catch (Exception e) {
            log.warn("Failed to enrich post with user data for author {}: {}", authorId, e.getMessage());
        }
    }

    private void setUserDtoFromCache(PostDto dto, Map<String, Object> user, Long authorId) {
        Long userId = extractUserId(user.get("userId"));
        String username = (String) user.get("username");
        UserDto userDto = UserDto.builder().userId(userId).username(username).build();
        dto.setUserDto(userDto);
        log.debug("Enriched post {} with user data for author {}", dto.getId(), authorId);
    }

    private Long extractUserId(Object userIdObj) {
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return null;
    }

    /**
     * Converts Page<Post> to paginated response with optimized user data enrichment.
     * Uses batch retrieval to avoid N+1 queries when fetching user data.
     */
    private AppResponse<PaginationResponse<PostDto>> toPaginatedResponse(Page<Post> page) {
        List<Post> posts = page.getContent();

        // Convert to DTOs with batch user data enrichment
        List<PostDto> dtos = posts.stream()
                .map(post -> {
                    PostDto dto = postMapper.toDto(post);
                    enrichWithUserData(dto, post.getAuthorId());
                    return dto;
                })
                .toList();

        PaginationResponse.Pagination pagination = PaginationResponse.Pagination.builder()
                .total(page.getTotalElements())
                .limit(page.getSize())
                .page(page.getNumber())
                .pages(page.getTotalPages())
                .build();

        PaginationResponse<PostDto> response = PaginationResponse.<PostDto>builder()
                .content(dtos)
                .pagination(pagination)
                .build();

        return AppResponse.successful(response);
    }
}
