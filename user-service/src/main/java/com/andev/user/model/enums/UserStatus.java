package com.andev.user.model.enums;

/**
 * User status enumeration
 */
public enum UserStatus {
    ACTIVE("Active user"),
    INACTIVE("Inactive user"),
    SUSPENDED("Suspended user"),
    PENDING("Pending activation"),
    DELETED("Deleted user");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
