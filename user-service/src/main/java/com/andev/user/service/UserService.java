package com.andev.user.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.andev.user.model.domain.dto.UserDto;
import com.andev.user.model.domain.dto.request.UserRequestDto;
import com.andev.user.model.domain.dto.request.UserUpdateRequestDto;
import com.andev.user.model.entities.User;
import com.andev.user.model.enums.UserStatus;

/**
 * Service interface for User operations
 */
public interface UserService extends BaseService<User, UserDto, Long> {

    /**
     * Create new user
     */
    UserDto createUser(UserRequestDto requestDto);

    /**
     * Update user
     */
    UserDto updateUser(Long id, UserUpdateRequestDto updateDto);

    /**
     * Find user by email
     */
    Optional<UserDto> findByEmail(String email);

    /**
     * Find user by nickname
     */
    Optional<UserDto> findByNickname(String nickname);

    /**
     * Find users by status
     */
    List<UserDto> findByStatus(UserStatus status);

    /**
     * Find users by status with pagination
     */
    Page<UserDto> findByStatus(UserStatus status, Pageable pageable);

    /**
     * Search users by email pattern
     */
    List<UserDto> searchByEmail(String emailPattern);

    /**
     * Search users by nickname pattern
     */
    List<UserDto> searchByNickname(String nicknamePattern);

    /**
     * Activate user
     */
    UserDto activateUser(Long id);

    /**
     * Deactivate user
     */
    UserDto deactivateUser(Long id);

    /**
     * Suspend user
     */
    UserDto suspendUser(Long id);

    /**
     * Soft delete user
     */
    UserDto softDeleteUser(Long id);

    /**
     * Hard delete user
     */
    void hardDeleteUser(Long id);

    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Check if user exists by nickname
     */
    boolean existsByNickname(String nickname);

    /**
     * Search users with RSQL query
     */
    List<UserDto> searchWithRsql(String rsqlQuery);
}
