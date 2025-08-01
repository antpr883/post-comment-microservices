package com.andev.post.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andev.cache.service.UserCacheService;
import com.andev.post.exception.NotFoundException;
import com.andev.post.model.constants.ApiErrorMessage;
import com.andev.post.model.domain.dto.PostDto;
import com.andev.post.model.domain.dto.UserDto;
import com.andev.post.model.domain.dto.request.PostRequestDto;
import com.andev.post.model.domain.mapper.PostMapper;
import com.andev.post.model.entities.Post;
import com.andev.post.repository.PostRepository;
import com.andev.post.service.PostService;
import com.andev.post.web.response.AppResponse;
import com.andev.post.web.response.PaginationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final PostRepository postRepository;

    @Autowired(required = false)
    private UserCacheService userCacheService;

    @Override
    public AppResponse<PostDto> findById(Long id) {
        Post post = postRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.RESOURCE_NOT_FOUND_BY_ID.getMessage(id)));

        PostDto dto = postMapper.toDto(post);

        // Try to get user data from cache if available
        if (post.getAuthorId() != null && userCacheService != null) {
            Optional<Map<String, Object>> userData = userCacheService.getUserById(post.getAuthorId());
            if (userData.isPresent()) {
                log.info("User data found in cache for author ID: {}", post.getAuthorId());
                Object userIdObj = userData.get().get("userId");
                Long userId = null;
                if (userIdObj instanceof Integer) {
                    userId = ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof Long) {
                    userId = (Long) userIdObj;
                }

                UserDto userDto = UserDto.builder()
                        .userId(userId)
                        .username((String) userData.get().get("username"))
                        .build();
                dto.setUserDto(userDto);
            } else {
                log.debug("User data not found in cache for author ID: {}", post.getAuthorId());
            }
        }

        return AppResponse.successful(dto);
    }

    @Override
    public AppResponse<PaginationResponse<PostDto>> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public AppResponse<PaginationResponse<PostDto>> findByAuthorId(Long authorId, Pageable pageable) {
        return null;
    }

    @Override
    public AppResponse<PaginationResponse<PostDto>> findByIds(List<Long> ids, Pageable pageable) {
        return null;
    }

    @Override
    public AppResponse<PaginationResponse<PostDto>> findByStatus(String status, Pageable pageable) {
        return null;
    }

    @Override
    public AppResponse<PostDto> create(PostRequestDto requestDto) {
        return null;
    }

    @Override
    public AppResponse<PostDto> update(Long id, PostRequestDto requestDto) {
        return null;
    }

    @Override
    public void updateStatuses(List<Long> ids, String newStatus) {}

    @Override
    public AppResponse<PostDto> delete(Long id) {
        return null;
    }

    @Override
    public AppResponse<PostDto> softDelete(Long id) {
        return null;
    }
}
