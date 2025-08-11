package com.andev.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.andev.user.model.entities.Role;

/**
 * Repository interface for Role entity
 */
@Repository
public interface RoleRepository extends CustomJpaRepository<Role, Long> {

    /**
     * Find role by name
     */
    Optional<Role> findByName(String name);

    /**
     * Find role by user ID
     */
    @Query("SELECT u.role FROM User u WHERE u.id = :userId")
    Optional<Role> findByUserId(@Param("userId") Long userId);

    /**
     * Check if role exists by name
     */
    boolean existsByName(String name);

    /**
     * Find roles by name pattern with pagination
     */
    @Query("SELECT r FROM Role r WHERE r.name LIKE %:namePattern%")
    Page<Role> findByNamePattern(@Param("namePattern") String namePattern, Pageable pageable);

    /**
     * Find roles by description pattern with pagination
     */
    @Query("SELECT r FROM Role r WHERE r.description LIKE %:descriptionPattern%")
    Page<Role> findByDescriptionPattern(@Param("descriptionPattern") String descriptionPattern, Pageable pageable);

    /**
     * Find all roles with pagination
     */
    Page<Role> findAll(Pageable pageable);
}
