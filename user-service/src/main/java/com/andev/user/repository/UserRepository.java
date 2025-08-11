package com.andev.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.andev.user.model.entities.User;
import com.andev.user.model.enums.UserStatus;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends CustomJpaRepository<User, Long> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by nickname
     */
    Optional<User> findByNickname(String nickname);

    /**
     * Find users by status
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Find users by status with pagination
     */
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Check if user exists by nickname
     */
    boolean existsByNickname(String nickname);

    /**
     * Find users with roles using EntityGraph
     */
    @EntityGraph(attributePaths = {"role"})
    @Query("SELECT u FROM User u WHERE u.status != :deletedStatus")
    Page<User> findAllWithRoles(Pageable pageable, @Param("deletedStatus") UserStatus deletedStatus);

    /**
     * Find user by ID with roles using EntityGraph
     */
    @EntityGraph(attributePaths = {"role"})
    Optional<User> findById(Long id);

    /**
     * Find users by email pattern
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:emailPattern% AND u.status != :deletedStatus")
    List<User> findByEmailPattern(
            @Param("emailPattern") String emailPattern, @Param("deletedStatus") UserStatus deletedStatus);

    /**
     * Find users by nickname pattern
     */
    @Query("SELECT u FROM User u WHERE u.nickname LIKE %:nicknamePattern% AND u.status != :deletedStatus")
    List<User> findByNicknamePattern(
            @Param("nicknamePattern") String nicknamePattern, @Param("deletedStatus") UserStatus deletedStatus);
}
