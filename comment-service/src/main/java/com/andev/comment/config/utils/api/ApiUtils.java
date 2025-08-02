package com.andev.comment.config.utils.api;

import com.andev.comment.config.model.constants.api.ApiConstants;

public class ApiUtils {

    public String getMethodName() {
        try {
            return Thread.currentThread().getStackTrace()[1].getMethodName();
        } catch (Exception e) {
            return ApiConstants.UNDEFINED;
        }
    }
}
