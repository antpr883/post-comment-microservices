package com.andev.post.model.domain.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AuditDto extends BaseDto {

    @Schema(description = "User who created the entity", example = "john_doe")
    private String createdBy;

    @Schema(description = "User who updated the entity", example = "john_doe")
    private String modifiedBy;

    @Schema(description = "Creation timestamp", example = "2025-01-01T12:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime created;

    @Schema(description = "Last modification timestamp", example = "2025-01-02T14:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updated;
}
