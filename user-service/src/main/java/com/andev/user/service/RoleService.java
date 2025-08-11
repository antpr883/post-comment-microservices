package com.andev.user.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.andev.user.model.domain.dto.RoleDto;
import com.andev.user.model.domain.dto.request.RoleRequestDto;
import com.andev.user.model.entities.Role;

/**
 * Service interface for Role operations
 */
public interface RoleService extends BaseService<Role, RoleDto, Long> {

    /**
     * Create new role
     */
    RoleDto createRole(RoleRequestDto requestDto);

    /**
     * Find role by name
     */
    Optional<RoleDto> findByName(String name);

    /**
     * Find role by user ID
     */
    Optional<RoleDto> findByUserId(Long userId);

    /**
     * Search roles by name pattern with pagination
     */
    Page<RoleDto> searchByName(String namePattern, Pageable pageable);

    /**
     * Search roles by description pattern with pagination
     */
    Page<RoleDto> searchByDescription(String descriptionPattern, Pageable pageable);

    /**
     * Check if role exists by name
     */
    boolean existsByName(String name);
}
