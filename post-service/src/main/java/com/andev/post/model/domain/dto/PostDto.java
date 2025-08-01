package com.andev.post.model.domain.dto;

import java.util.LinkedHashSet;
import java.util.Set;

import com.andev.post.model.enums.PostStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDto extends AuditDto {
    private UserDto userDto;
    private JsonNode description;
    private String title;
    private String content;
    private PostStatus status;
    private Integer likes = 0;
    private Set<Long> commentsIds = new LinkedHashSet<>();
}
