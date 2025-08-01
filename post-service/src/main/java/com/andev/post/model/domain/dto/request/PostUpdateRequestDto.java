package com.andev.post.model.domain.dto.request;

import java.util.LinkedHashSet;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequestDto extends PostRequestDto {

    @NotNull(message = "Post ID is required for updates")
    @Schema(description = "ID of the post to update", example = "1", required = true)
    private Long id;

    @Min(value = 0, message = "Likes cannot be negative")
    @Builder.Default
    @Schema(description = "Number of likes", example = "5")
    private Integer likes = 0;

    @Builder.Default
    @Schema(description = "Set of comment IDs associated with this post")
    private Set<Long> commentsIds = new LinkedHashSet<>();
}
