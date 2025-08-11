package com.andev.user.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andev.user.exception.DataExistException;
import com.andev.user.exception.NotFoundException;
import com.andev.user.model.domain.dto.UserDto;
import com.andev.user.model.domain.dto.request.UserRequestDto;
import com.andev.user.model.domain.dto.request.UserUpdateRequestDto;
import com.andev.user.model.domain.mapper.RoleMapper;
import com.andev.user.model.domain.mapper.UserMapper;
import com.andev.user.model.entities.Role;
import com.andev.user.model.entities.User;
import com.andev.user.model.enums.UserStatus;
import com.andev.user.repository.RoleRepository;
import com.andev.user.repository.UserRepository;
import com.andev.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of UserService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public Optional<UserDto> findById(Long id) {
        log.info("Finding user by id: {}", id);
        return userRepository.findById(id).map(user -> userMapper.toDtoWithRoles(user, roleMapper));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        log.info("Finding all users");
        return userRepository.findAll().stream()
                .map(user -> userMapper.toDtoWithRoles(user, roleMapper))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        log.info("Finding all users with pagination: {}", pageable);
        return userRepository
                .findAllWithRoles(pageable, UserStatus.DELETED)
                .map(user -> userMapper.toDtoWithRoles(user, roleMapper));
    }

    @Override
    public UserDto save(UserDto dto) {
        log.info("Saving user: {}", dto.getEmail());
        User user = userMapper.toEntity(dto);
        user = userRepository.save(user);
        return userMapper.toDtoWithRoles(user, roleMapper);
    }

    @Override
    public UserDto update(Long id, UserDto dto) {
        log.info("Updating user with id: {}", id);
        User user =
                userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        // For update method, we need to manually copy fields since we don't have UserUpdateRequestDto
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        user = userRepository.save(user);
        return userMapper.toDtoWithRoles(user, roleMapper);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public UserDto createUser(UserRequestDto requestDto) {
        log.info("Creating new user: {}", requestDto.getEmail());

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new DataExistException("User with email " + requestDto.getEmail() + " already exists");
        }

        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new DataExistException("User with nickname " + requestDto.getNickname() + " already exists");
        }

        User user = userMapper.toEntity(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        // Assign role if provided
        if (requestDto.getRoleIds() != null && !requestDto.getRoleIds().isEmpty()) {
            Long roleId = requestDto.getRoleIds().iterator().next(); // Take first role
            Role role = roleRepository
                    .findById(roleId)
                    .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));
            user.setRole(role);
        }

        user = userRepository.save(user);

        return userMapper.toDtoWithRoles(user, roleMapper);
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public UserDto updateUser(Long id, UserUpdateRequestDto updateDto) {
        log.info("Updating user with id: {}", id);

        User user =
                userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        userMapper.updateEntity(user, updateDto);

        if (updateDto.getPassword() != null && !updateDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        // Update role if provided
        if (updateDto.getRoleIds() != null && !updateDto.getRoleIds().isEmpty()) {
            Long roleId = updateDto.getRoleIds().iterator().next(); // Take first role
            Role role = roleRepository
                    .findById(roleId)
                    .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));
            user.setRole(role);
        }

        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'email:' + #email")
    public Optional<UserDto> findByEmail(String email) {
        log.info("Finding user by email: {}", email);
        return userRepository.findByEmail(email).map(user -> userMapper.toDtoWithRoles(user, roleMapper));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findByNickname(String nickname) {
        log.info("Finding user by nickname: {}", nickname);
        return userRepository.findByNickname(nickname).map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findByStatus(UserStatus status) {
        log.info("Finding users by status: {}", status);
        return userRepository.findByStatus(status).stream()
                .map(user -> userMapper.toDtoWithRoles(user, roleMapper))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findByStatus(UserStatus status, Pageable pageable) {
        log.info("Finding users by status: {} with pagination: {}", status, pageable);
        return userRepository.findByStatus(status, pageable).map(user -> userMapper.toDtoWithRoles(user, roleMapper));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> searchByEmail(String emailPattern) {
        log.info("Searching users by email pattern: {}", emailPattern);
        return userRepository.findByEmailPattern(emailPattern, UserStatus.DELETED).stream()
                .map(user -> userMapper.toDtoWithRoles(user, roleMapper))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> searchByNickname(String nicknamePattern) {
        log.info("Searching users by nickname pattern: {}", nicknamePattern);
        return userRepository.findByNicknamePattern(nicknamePattern, UserStatus.DELETED).stream()
                .map(user -> userMapper.toDtoWithRoles(user, roleMapper))
                .toList();
    }

    @Override
    public UserDto activateUser(Long id) {
        log.info("Activating user with id: {}", id);
        User user =
                userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);
        return userMapper.toDtoWithRoles(user, roleMapper);
    }

    @Override
    public UserDto deactivateUser(Long id) {
        log.info("Deactivating user with id: {}", id);
        User user =
                userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        user.setStatus(UserStatus.INACTIVE);
        user = userRepository.save(user);
        return userMapper.toDtoWithRoles(user, roleMapper);
    }

    @Override
    public UserDto suspendUser(Long id) {
        log.info("Suspending user with id: {}", id);
        User user =
                userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        user.setStatus(UserStatus.SUSPENDED);
        user = userRepository.save(user);
        return userMapper.toDtoWithRoles(user, roleMapper);
    }

    @Override
    public UserDto softDeleteUser(Long id) {
        log.info("Soft deleting user with id: {}", id);
        User user =
                userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        user.setStatus(UserStatus.DELETED);
        user = userRepository.save(user);
        return userMapper.toDtoWithRoles(user, roleMapper);
    }

    @Override
    public void hardDeleteUser(Long id) {
        log.info("Hard deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> searchWithRsql(String rsqlQuery) {
        log.info("Searching users with RSQL query: {}", rsqlQuery);
        // TODO: Implement RSQL search using RsqlParserService
        // For now, return all users
        return findAll();
    }
}
