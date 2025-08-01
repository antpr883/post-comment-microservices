package com.andev.post.model.domain.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * Data Transfer Object for User information used in caching operations.
 * This DTO represents user data that flows between Redis, PostgreSQL, and User Service API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class UserDto implements Serializable {

    @JsonProperty("userId")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long userId;

    @JsonProperty("username")
    @ToString.Include
    private String username;
}
