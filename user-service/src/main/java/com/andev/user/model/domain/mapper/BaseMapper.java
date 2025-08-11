package com.andev.user.model.domain.mapper;

import java.util.List;

import org.mapstruct.Mapping;

import com.andev.user.model.domain.dto.BaseDto;
import com.andev.user.model.entities.PersistenceModel;

/**
 * Base mapper interface for entity-DTO conversion
 */
public interface BaseMapper<E extends PersistenceModel, D extends BaseDto> {

    /**
     * Convert entity to DTO
     */
    @Mapping(target = "createdAt", source = "created")
    @Mapping(target = "updatedAt", source = "updated")
    D toDto(E entity);

    /**
     * Convert DTO to entity
     */
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    E toEntity(D dto);

    /**
     * Convert list of entities to list of DTOs
     */
    List<D> toDtoList(List<E> entities);

    /**
     * Convert list of DTOs to list of entities
     */
    List<E> toEntityList(List<D> dtos);
}
