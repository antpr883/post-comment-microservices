package com.andev.post.service;

import com.andev.post.model.domain.dto.DtoMarker;
import com.andev.post.web.response.AppResponse;

public interface BaseService<D extends DtoMarker, R extends DtoMarker> {

    AppResponse<D> create(R requestDto);

    AppResponse<D> delete(Long id);

    AppResponse<D> softDelete(Long id);

    AppResponse<D> findById(Long id);
}
