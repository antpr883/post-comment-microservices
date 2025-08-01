package com.andev.cache.model.domain.mapper;


import com.andev.cache.model.domain.dto.UserCacheDto;
import com.andev.cache.model.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting between User entity and UserCacheDto.
 * Uses MapStruct for automatic implementation generation.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * Converts User entity to UserCacheDto.
     * 
     * @param user the User entity
     * @return the UserCacheDto
     */
    UserCacheDto toDto(User user);

    /**
     * Converts UserCacheDto to User entity.
     * 
     * @param userCacheDto the UserCacheDto
     * @return the User entity
     */
    @Mapping(target = "userId", source = "userCacheDto.userId")
    @Mapping(target = "username", source = "userCacheDto.username")
    @Mapping(target = "cachedAt", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    User toEntity(UserCacheDto userCacheDto);

    /**
     * Updates existing User entity with data from UserCacheDto.
     * 
     * @param userCacheDto the UserCacheDto with updated data
     * @param user the existing User entity to update
     * @return the updated User entity
     */
    @Mapping(target = "userId", source = "userCacheDto.userId")
    @Mapping(target = "username", source = "userCacheDto.username")
    @Mapping(target = "cachedAt", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    User updateEntityFromDto(UserCacheDto userCacheDto, User user);
} 