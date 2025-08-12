package com.andev.comment.utils.api;

import com.andev.comment.model.constants.ApiConstants;

public class ApiUtils {

    public String getMethodName() {
        try {
            return Thread.currentThread().getStackTrace()[1].getMethodName();
        } catch (Exception e) {
            return ApiConstants.UNDEFINED;
        }
    }
}
