package com.andev.post.web.response;

import java.io.Serializable;

import com.andev.post.model.constants.ApiConstants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppResponse<P extends Serializable> implements Serializable {
    private String message;
    private P payload;
    private boolean success;

    public static <P extends Serializable> AppResponse<P> successful(P payload) {
        return new AppResponse<>(ApiConstants.RESOURCE_FOUNDED, payload, true);
    }
}
