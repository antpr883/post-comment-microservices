package com.andev.post.model.domain.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.andev.post.model.domain.dto.PostDto;
import com.andev.post.model.domain.dto.request.PostRequestDto;
import com.andev.post.model.domain.dto.request.PostUpdateRequestDto;
import com.andev.post.model.entities.Post;

@Component
public class PostMapper implements BaseMapper<Post, PostDto, PostRequestDto, PostUpdateRequestDto> {

    @Override
    public PostDto toDto(Post entity) {
        if (entity == null) {
            return null;
        }

        return PostDto.builder()
                .id(entity.getId())
                .createdBy(entity.getCreatedBy())
                .modifiedBy(entity.getModifiedBy())
                .created(entity.getCreated())
                .updated(entity.getUpdated())
                .title(entity.getTitle())
                .content(entity.getContent())
                .description(entity.getDescription())
                .status(entity.getPostStatus())
                .likes(entity.getLikes())
                .commentsIds(entity.getCommentsIds())
                .build();
    }

    @Override
    public Post toEntity(PostRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }

        return Post.builder()
                .authorId(requestDto.getAuthorId())
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .description(requestDto.getDescription())
                .build();
    }

    @Override
    public Set<PostDto> toDtoList(Set<Post> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream().map(this::toDto).collect(Collectors.toSet());
    }

    @Override
    public Set<Post> toEntityList(Set<PostRequestDto> requestDtos) {
        if (requestDtos == null) {
            return null;
        }

        return requestDtos.stream().map(this::toEntity).collect(Collectors.toSet());
    }

    @Override
    public void updateEntity(PostUpdateRequestDto postUpdateRequestDto, Post entity) {
        if (postUpdateRequestDto == null || entity == null) {
            return;
        }

        if (postUpdateRequestDto.getAuthorId() != null) {
            entity.setAuthorId(postUpdateRequestDto.getAuthorId());
        }
        if (postUpdateRequestDto.getTitle() != null) {
            entity.setTitle(postUpdateRequestDto.getTitle());
        }
        if (postUpdateRequestDto.getContent() != null) {
            entity.setContent(postUpdateRequestDto.getContent());
        }
        if (postUpdateRequestDto.getDescription() != null) {
            entity.setDescription(postUpdateRequestDto.getDescription());
        }
    }
}
