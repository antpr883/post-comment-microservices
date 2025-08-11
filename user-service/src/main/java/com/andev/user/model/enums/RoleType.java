package com.andev.user.model.enums;

/**
 * Role type enumeration
 */
public enum RoleType {
    ADMIN("Administrator"),
    MODERATOR("Moderator"),
    USER("Regular user"),
    GUEST("Guest user");

    private final String description;

    RoleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
