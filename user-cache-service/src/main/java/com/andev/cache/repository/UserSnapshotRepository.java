package com.andev.cache.repository;

import com.andev.cache.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserSnapshotRepository extends JpaRepository<User, Long> {
    

    /**
     * Deletes expired users from the database.
     * 
     * @param expirationTime the time before which users are considered expired
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM User u WHERE u.expiresAt < :expirationTime")
    int deleteExpiredUsers(@Param("expirationTime") LocalDateTime expirationTime);


    /**
     * Find user data as Map by ID (legacy method for compatibility)
     */
    default Map<String, Object> findUserDataById(Long userId) {
        return findById(userId)
                .map(user -> {
                    // Fallback to basic fields if userData is not available
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", user.getUserId());
                    userData.put("username", user.getUsername());
                    userData.put("cachedAt", user.getCachedAt());
                    userData.put("expiresAt", user.getExpiresAt());
                    return userData;
                })
                .orElse(null);
    }
}
