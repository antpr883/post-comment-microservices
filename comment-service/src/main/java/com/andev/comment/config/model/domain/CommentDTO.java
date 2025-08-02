package com.andev.comment.config.model.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO implements Serializable {
    private String id;
    private String postId;
    private String content;
    private String authorId;
    private UserDto user;
    private String parentCommentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer likesCount;
    private Integer repliesCount;
    private Boolean isDeleted;
    private List<CommentDTO> replies;
}
