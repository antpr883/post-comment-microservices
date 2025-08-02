package com.andev.comment.model.constants.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApiErrorMessage {
    COMMENT_NOT_FOUND("Comment not found with id: %s");
    private final String message;

    public String getMessage(Object... args) {
        return String.format(message, args);
    }
}
