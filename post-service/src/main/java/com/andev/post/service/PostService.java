package com.andev.post.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.andev.post.model.domain.dto.PostDto;
import com.andev.post.model.domain.dto.request.PostRequestDto;
import com.andev.post.model.domain.dto.request.PostUpdateRequestDto;
import com.andev.post.web.response.AppResponse;
import com.andev.post.web.response.PaginationResponse;

public interface PostService extends BaseService<PostDto, PostRequestDto> {

    AppResponse<PaginationResponse<PostDto>> findAll(Pageable pageable);

    AppResponse<PaginationResponse<PostDto>> findByAuthorId(Long authorId, Pageable pageable);

    AppResponse<PaginationResponse<PostDto>> findByIds(List<Long> ids, Pageable pageable);

    AppResponse<PaginationResponse<PostDto>> findByStatus(String status, Pageable pageable);

    AppResponse<PostDto> update(Long id, PostUpdateRequestDto requestUpdateDto);

    void updateStatuses(List<Long> ids, String newStatus);

    /**
     * Search posts using RSQL query with pagination support
     *
     * @param rsqlQuery RSQL query string (e.g., "title==*test*;authorId==123;description.author==John")
     * @param pageable pagination parameters
     * @return paginated response with filtered posts
     */
    AppResponse<PaginationResponse<PostDto>> search(String rsqlQuery, Pageable pageable);
}
