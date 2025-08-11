package com.andev.user.model.constants.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * API constants for User Service
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiConstants {

    public static final String API_BASE_PATH = "/api/v1";
    public static final String USERS_PATH = "/users";
    public static final String ROLES_PATH = "/roles";

    public static final String RESOURCE_FOUNDED = "Resource founded";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_SORT_FIELD = "id";
    public static final String DEFAULT_SORT_DIRECTION = "ASC";

    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String BREAK_LINE = "\n";
    public static final String TIME_ZONE_PACKAGE_NAME = "java.time.zone";

    public static final String UNDEFINED = "undefined";
}
