package com.andev.user.model.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiConstants {

    public static final String RESOURCE_FOUNDED = "Resource founded";
    public static final String UNDEFINED = "undefined";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String BREAK_LINE = "\n";
    public static final String TIME_ZONE_PACKAGE_NAME = "java.time.zone";
    public static final String DASH = "-";
    public static final String PASSWORD_ALL_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";
    public static final String PASSWORD_LETTERS_UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String PASSWORD_LETTERS_LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
    public static final String PASSWORD_DIGITS = "0123456789";
    public static final String PASSWORD_CHARACTERS = "~`!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";
    public static final Integer REQUIRED_MIN_PASSWORD_LENGTH = 8;
    public static final Integer REQUIRED_MIN_LETTERS_NUMBER_EVERY_CASE_IN_PASSWORD = 1;
    public static final Integer REQUIRED_MIN_DIGITS_NUMBER_IN_PASSWORD = 1;
    public static final Integer REQUIRED_MIN_CHARACTERS_NUMBER_IN_PASSWORD = 1;

    // API Paths
    public static final String API_BASE_PATH = "/api/v1";
    public static final String USERS_PATH = "/users";
    public static final String ROLES_PATH = "/roles";

    // Pagination defaults
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_FIELD = "id";
    public static final String DEFAULT_SORT_DIRECTION = "ASC";
}
