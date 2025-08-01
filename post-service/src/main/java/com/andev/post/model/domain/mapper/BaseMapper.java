package com.andev.post.model.domain.mapper;

import java.util.Set;

import org.mapstruct.MappingTarget;

public interface BaseMapper<E, D, R, U> {

    D toDto(E entity);

    E toEntity(R requestDto);

    Set<E> toEntityList(Set<R> requestDtos);

    Set<D> toDtoList(Set<E> entities);

    void updateEntity(U postRequestDto, @MappingTarget E entity);
}
