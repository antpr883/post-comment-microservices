package com.andev.user.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andev.user.exception.DataExistException;
import com.andev.user.exception.NotFoundException;
import com.andev.user.model.domain.dto.RoleDto;
import com.andev.user.model.domain.dto.request.RoleRequestDto;
import com.andev.user.model.domain.mapper.RoleMapper;
import com.andev.user.model.entities.Role;
import com.andev.user.repository.RoleRepository;
import com.andev.user.service.RoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of RoleService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<RoleDto> findById(Long id) {
        log.info("Finding role by id: {}", id);
        return roleRepository.findById(id).map(roleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> findAll() {
        log.info("Finding all roles");
        return roleMapper.toDtoList(roleRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDto> findAll(Pageable pageable) {
        log.info("Finding all roles with pagination: {}", pageable);
        return roleRepository.findAll(pageable).map(roleMapper::toDto);
    }

    @Override
    public RoleDto save(RoleDto dto) {
        log.info("Saving role: {}", dto.getName());
        Role role = roleMapper.toEntity(dto);
        role = roleRepository.save(role);
        return roleMapper.toDto(role);
    }

    @Override
    public RoleDto update(Long id, RoleDto dto) {
        log.info("Updating role with id: {}", id);
        Role role =
                roleRepository.findById(id).orElseThrow(() -> new NotFoundException("Role not found with id: " + id));

        if (dto.getName() != null) role.setName(dto.getName());
        if (dto.getDescription() != null) role.setDescription(dto.getDescription());

        role = roleRepository.save(role);
        return roleMapper.toDto(role);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Deleting role with id: {}", id);
        if (!roleRepository.existsById(id)) {
            throw new NotFoundException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return roleRepository.existsById(id);
    }

    @Override
    public RoleDto createRole(RoleRequestDto requestDto) {
        log.info("Creating new role: {}", requestDto.getName());

        if (roleRepository.existsByName(requestDto.getName())) {
            throw new DataExistException("Role with name " + requestDto.getName() + " already exists");
        }

        Role role = roleMapper.toEntity(requestDto);
        role = roleRepository.save(role);
        return roleMapper.toDto(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoleDto> findByName(String name) {
        log.info("Finding role by name: {}", name);
        return roleRepository.findByName(name).map(roleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoleDto> findByUserId(Long userId) {
        log.info("Finding role by user id: {}", userId);
        return roleRepository.findByUserId(userId).map(roleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDto> searchByName(String namePattern, Pageable pageable) {
        log.info("Searching roles by name pattern: {} with pagination: {}", namePattern, pageable);
        return roleRepository.findByNamePattern(namePattern, pageable).map(roleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDto> searchByDescription(String descriptionPattern, Pageable pageable) {
        log.info("Searching roles by description pattern: {} with pagination: {}", descriptionPattern, pageable);
        return roleRepository
                .findByDescriptionPattern(descriptionPattern, pageable)
                .map(roleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
}
