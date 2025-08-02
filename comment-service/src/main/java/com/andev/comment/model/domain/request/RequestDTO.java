package com.andev.comment.model.domain.request;

import java.io.Serializable;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO implements Serializable {

    private String postId;
    private String content;
    private String authorId;
    private String parentCommentId;
    private Integer likesCount;
    private Integer repliesCount;
    private Boolean isDeleted;
}
