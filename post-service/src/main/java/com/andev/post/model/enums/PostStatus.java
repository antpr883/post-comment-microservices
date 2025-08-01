package com.andev.post.model.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum PostStatus {
    ACTIVE,
    INACTIVE,
    CLOSED,
    MODERATED,
    DELETED
}
