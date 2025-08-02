package com.andev.comment.config.model.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * Data Transfer Object for User information used in caching operations.
 * This DTO represents user data that flows between Redis, PostgreSQL, and User Service API.
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserDto implements Serializable {

    @JsonProperty("userId")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long userId;

    @JsonProperty("username")
    @ToString.Include
    private String username;
}
