package com.andev.user.model.domain.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.andev.user.model.domain.dto.RoleDto;
import com.andev.user.model.domain.dto.request.RoleRequestDto;
import com.andev.user.model.entities.Role;

/**
 * Mapper for Role entity and DTOs using MapStruct
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleMapper {

    /**
     * Convert entity to DTO
     */
    RoleDto toDto(Role entity);

    /**
     * Convert DTO to entity
     */
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    Role toEntity(RoleDto dto);

    /**
     * Convert list of entities to list of DTOs
     */
    List<RoleDto> toDtoList(List<Role> entities);

    /**
     * Convert list of DTOs to list of entities
     */
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    List<Role> toEntityList(List<RoleDto> dtos);

    /**
     * Convert RoleRequestDto to Role entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    Role toEntity(RoleRequestDto requestDto);

    /**
     * Convert list of RoleRequestDto to list of Role entities
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    List<Role> toEntityListFromRequest(List<RoleRequestDto> requestDtos);
}
