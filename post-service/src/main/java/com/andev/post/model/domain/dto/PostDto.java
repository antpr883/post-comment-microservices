package com.andev.post.model.domain.dto;

import java.util.LinkedHashSet;
import java.util.Set;

import com.andev.post.model.enums.PostStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Post data transfer object")
public class PostDto extends AuditDto {

    @Schema(description = "Associated user information", example = "UserDto object")
    private UserDto userDto;

    @Schema(description = "Additional post metadata", example = "{\"summary\": \"Brief description\"}")
    private JsonNode description;

    @Schema(description = "Post title", example = "My First Post")
    private String title;

    @Schema(description = "Post content", example = "This is the content of my post")
    private String content;

    @Schema(description = "Post status", example = "ACTIVE")
    private PostStatus status;

    @Schema(description = "Number of likes", example = "10")
    @Builder.Default
    private Integer likes = 0;

    @Schema(description = "Set of comment IDs associated with this post", example = "[1, 2, 3]")
    @Builder.Default
    private Set<Long> commentsIds = new LinkedHashSet<>();
}
