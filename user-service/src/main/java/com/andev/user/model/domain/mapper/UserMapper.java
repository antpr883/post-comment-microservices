package com.andev.user.model.domain.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.andev.user.model.domain.dto.UserDto;
import com.andev.user.model.domain.dto.request.UserRequestDto;
import com.andev.user.model.domain.dto.request.UserUpdateRequestDto;
import com.andev.user.model.entities.User;

/**
 * Mapper for User entity and DTOs using MapStruct
 */
@Mapper(
        componentModel = "spring",
        uses = {RoleMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    /**
     * Convert entity to DTO
     */
    @Mapping(target = "roles", ignore = true)
    UserDto toDto(User entity);

    /**
     * Convert DTO to entity
     */
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserDto dto);

    /**
     * Convert list of entities to list of DTOs
     */
    List<UserDto> toDtoList(List<User> entities);

    /**
     * Convert list of DTOs to list of entities
     */
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    List<User> toEntityList(List<UserDto> dtos);

    /**
     * Convert UserRequestDto to User entity
     */
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    User toEntity(UserRequestDto requestDto);

    /**
     * Update User entity from UserUpdateRequestDto
     */
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    void updateEntity(@MappingTarget User user, UserUpdateRequestDto updateDto);

    /**
     * Convert list of UserRequestDto to list of User entities
     */
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    List<User> toEntityListFromRequest(List<UserRequestDto> requestDtos);

    /**
     * Custom method to map user with roles
     */
    default UserDto toDtoWithRoles(User entity, RoleMapper roleMapper) {
        UserDto dto = toDto(entity);
        if (entity.getRole() != null) {
            dto.setRoles(Set.of(roleMapper.toDto(entity.getRole())));
        }
        return dto;
    }
}
