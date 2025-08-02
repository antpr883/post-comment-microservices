package com.andev.comment.config.model.entitie;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.*;

@Document(collection = "comments")
@CompoundIndexes({
    @CompoundIndex(name = "post_created_idx", def = "{'postId': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "parent_created_idx", def = "{'parentCommentId': 1, 'createdAt': 1}")
})
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class Comments {
    @Id
    @MongoId
    private String id;

    private String postId;
    private String content;
    private String userId; //
    private String parentCommentId;
    private Integer likesCount = 0;
    private Integer repliesCount = 0;
    private Boolean isDeleted = false;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
