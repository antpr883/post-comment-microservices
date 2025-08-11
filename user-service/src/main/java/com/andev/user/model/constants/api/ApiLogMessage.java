package com.andev.user.model.constants.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApiLogMessage {
    GET_COMMENT_BY_ID("Getting comment by id: %s"),
    ;
    private final String message;

    public String getMessage(Object... args) {
        return String.format(message, args);
    }
}
