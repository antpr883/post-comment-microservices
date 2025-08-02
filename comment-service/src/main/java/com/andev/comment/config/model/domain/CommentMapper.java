package com.andev.comment.config.model.domain;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.andev.comment.config.model.entitie.Comments;

/**
 * Mapper interface for converting between Comments entity and CommentDTO.
 * Uses MapStruct for automatic implementation generation.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    /**
     * Maps Comments entity to CommentDTO.
     *
     * @param comments the Comments entity
     * @return the mapped CommentDTO
     */
    @Mapping(target = "authorId", source = "userId")
    @Mapping(target = "user", ignore = true) // User will be set separately in service
    @Mapping(target = "replies", ignore = true) // Replies will be handled separately
    CommentDTO toDto(Comments comments);

    /**
     * Maps CommentDTO to Comments entity.
     *
     * @param commentDTO the CommentDTO
     * @return the mapped Comments entity
     */
    @Mapping(target = "userId", source = "authorId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comments toEntity(CommentDTO commentDTO);
}
