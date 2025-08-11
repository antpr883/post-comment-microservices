package com.andev.user.model.constants.api;

/**
 * API error messages for User Service
 */
public final class ApiErrorMessage {

    // User related errors
    public static final String USER_NOT_FOUND = "User not found with id: %s";
    public static final String USER_ALREADY_EXISTS = "User with email %s already exists";
    public static final String USER_INVALID_DATA = "Invalid user data provided";
    public static final String USER_DELETION_FAILED = "Failed to delete user with id: %s";

    // Role related errors
    public static final String ROLE_NOT_FOUND = "Role not found with id: %s";
    public static final String ROLE_ALREADY_EXISTS = "Role with name %s already exists";
    public static final String ROLE_INVALID_DATA = "Invalid role data provided";
    public static final String ROLE_DELETION_FAILED = "Failed to delete role with id: %s";

    // General errors
    public static final String INTERNAL_ERROR = "Internal server error occurred";
    public static final String VALIDATION_ERROR = "Validation error: %s";
    public static final String UNAUTHORIZED = "Unauthorized access";
    public static final String FORBIDDEN = "Access forbidden";

    private ApiErrorMessage() {
        // Utility class
    }
}
