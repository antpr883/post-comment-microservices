package com.andev.post.model.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.andev.post.model.domain.dto.PostDto;
import com.andev.post.model.domain.dto.request.PostRequestDto;
import com.andev.post.model.domain.dto.request.PostUpdateRequestDto;
import com.andev.post.model.entities.Post;

@Mapper(componentModel = "spring")
public interface PostMapper extends BaseMapper<Post, PostDto, PostRequestDto, PostUpdateRequestDto> {

    @Override
    @Mapping(target = "status", source = "postStatus")
    @Mapping(target = "userDto", ignore = true)
    PostDto toDto(Post entity);

    @Override
    @Mapping(target = "postStatus", source = "status")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "likes", constant = "0")
    @Mapping(target = "commentsIds", expression = "java(new java.util.LinkedHashSet<>())")
    Post toEntity(PostRequestDto requestDto);

    @Override
    @Mapping(target = "postStatus", source = "status")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "commentsIds", ignore = true)
    void updateEntity(PostUpdateRequestDto postUpdateRequestDto, @MappingTarget Post entity);
}
