package com.andev.post.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.andev.post.model.domain.dto.PostDto;
import com.andev.post.model.domain.dto.request.PostRequestDto;
import com.andev.post.web.response.AppResponse;
import com.andev.post.web.response.PaginationResponse;

public interface PostService extends BaseService<PostDto, PostRequestDto> {

    void updateStatuses(List<Long> ids, String newStatus);

    AppResponse<PaginationResponse<PostDto>> findAll(Pageable pageable);

    AppResponse<PaginationResponse<PostDto>> findByAuthorId(Long authorId, Pageable pageable);

    AppResponse<PaginationResponse<PostDto>> findByIds(List<Long> ids, Pageable pageable);

    AppResponse<PaginationResponse<PostDto>> findByStatus(String status, Pageable pageable);
}
