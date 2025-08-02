package com.andev.cache.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * JPA Entity representing user data stored in the snapshot database.
 * Used for caching user information with TTL support.
 */
@Entity
@Table(
        name = "users",
        schema = "v1_snap_user",
        indexes = {@Index(name = "idx_users_user_id", columnList = "user_id", unique = true)})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User {
    private static final Logger log = LoggerFactory.getLogger(User.class);

    @Id
    @Column(name = "user_id", unique = true, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long userId;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    @ToString.Include
    private String username;

    @Column(name = "cached_at")
    private LocalDateTime cachedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (cachedAt == null) {
            cachedAt = now;
            log.debug("Setting cache start date for user {} to {}", userId, cachedAt);
        }

        if (expiresAt == null) {
            // Default 6 hours cache TTL
            expiresAt = now.plusHours(6);
            log.debug("Setting cache end date for user {} to {}", userId, expiresAt);
        }
    }
}
