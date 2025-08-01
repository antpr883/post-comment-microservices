package com.andev.post.model.entities;

import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.andev.post.model.enums.PostStatus;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "posts",
        indexes = {
            @Index(name = "idx_posts_author_id", columnList = "author_id"),
            @Index(name = "idx_posts_status", columnList = "post_status"),
            @Index(name = "idx_posts_created_at", columnList = "created_at")
        })
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Post extends PersistenceModel {

    @Column(name = "author_id", nullable = false)
    @ToString.Include
    private Long authorId;

    @Column(nullable = false, unique = true, length = 255)
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    @NotBlank(message = "Title is required")
    @ToString.Include
    @EqualsAndHashCode.Include
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Content is required")
    @Size(min = 10, message = "Content must be at least 10 characters")
    @ToString.Include
    private String content;

    @Column(name = "description", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode description;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_status", nullable = false, length = 20)
    @Builder.Default
    @ToString.Include
    private PostStatus postStatus = PostStatus.ACTIVE;

    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    @Min(0)
    private Integer likes = 0;

    @Builder.Default
    @Column(name = "comment_ids", columnDefinition = "bigint[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private Set<Long> commentsIds = new LinkedHashSet<>();
}
