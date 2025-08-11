package com.andev.user.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.andev.user.model.domain.dto.BaseDto;
import com.andev.user.model.entities.PersistenceModel;

/**
 * Base service interface with common operations
 */
public interface BaseService<E extends PersistenceModel, D extends BaseDto, ID> {

    /**
     * Find entity by ID
     */
    Optional<D> findById(ID id);

    /**
     * Find all entities
     */
    List<D> findAll();

    /**
     * Find all entities with pagination
     */
    Page<D> findAll(Pageable pageable);

    /**
     * Save entity
     */
    D save(D dto);

    /**
     * Update entity
     */
    D update(ID id, D dto);

    /**
     * Delete entity by ID
     */
    void deleteById(ID id);

    /**
     * Check if entity exists by ID
     */
    boolean existsById(ID id);
}
