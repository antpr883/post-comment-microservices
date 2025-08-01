package com.andev.cache.service.impl;

import com.andev.cache.config.UserCacheProperties;
import com.andev.cache.repository.UserSnapshotRepository;
import com.andev.cache.service.DatabaseUserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DatabaseUserCacheServiceImpl implements DatabaseUserCacheService {

    private final UserSnapshotRepository userSnapshotRepository;
    private final UserCacheProperties properties;

    @Override
    public Optional<Map<String, Object>> getUserById(Long userId) {
        try {
            Map<String, Object> userData = userSnapshotRepository.findUserDataById(userId);
            if (userData != null && !userData.isEmpty()) {
                log.debug("User found in database cache: {}", userId);
                return Optional.of(userData);
            }
            
            log.debug("User not found in database cache: {}", userId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error retrieving user from database cache: {}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void cacheUser(Long userId, Map<String, Object> userData) {
        try {
            userSnapshotRepository.saveUserSnapshot(userId, userData);
            log.debug("User cached in database: {}", userId);
        } catch (Exception e) {
            log.error("Error caching user in database: {}", userId, e);
        }
    }

    @Override
    @Transactional
    public void evictUser(Long userId) {
        try {
            userSnapshotRepository.deleteById(userId);
            log.debug("User evicted from database cache: {}", userId);
        } catch (Exception e) {
            log.error("Error evicting user from database cache: {}", userId, e);
        }
    }

    @Override
    public boolean exists(Long userId) {
        try {
            Map<String, Object> userData = userSnapshotRepository.findUserDataById(userId);
            return userData != null && !userData.isEmpty();
        } catch (Exception e) {
            log.error("Error checking user existence in database: {}", userId, e);
            return false;
        }
    }
} 