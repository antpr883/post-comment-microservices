package com.andev.comment.config.web.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentsResponse<P extends Serializable> implements Serializable {
    private String message;
    private P paylaod;
    private boolean success;

    public static <P extends Serializable> CommentsResponse<P> success(P payload) {
        return new CommentsResponse<>("Operation successful", payload, true);
    }
}
