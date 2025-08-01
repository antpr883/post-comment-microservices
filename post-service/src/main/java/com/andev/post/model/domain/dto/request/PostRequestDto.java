package com.andev.post.model.domain.dto.request;

import com.andev.post.model.domain.dto.DtoMarker;
import com.andev.post.model.enums.PostStatus;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto extends DtoMarker {
    @NotNull(message = "Author ID is required")
    @Schema(description = "ID of the post author", example = "123", required = true)
    private Long authorId;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Schema(description = "Post title", example = "My First Post", required = true)
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 5000, message = "Content must be between 1 and 5000 characters")
    @Schema(description = "Post content", example = "This is the content of my post", required = true)
    private String content;

    @Schema(description = "Additional post metadata", example = "{\"summary\": \"Brief description\"}")
    private JsonNode description;

    @NotNull(message = "Status is required")
    @Schema(description = "Post status", example = "ACTIVE", required = true)
    private PostStatus status;
}
