package com.andev.cache.repository.snapshot;


import com.andev.cache.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserSnapshotRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds user by username.
     * 
     * @param username the username to search for
     * @return Optional containing User if found, empty otherwise
     */
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);
    
    /**
     * Deletes expired users from the database.
     * 
     * @param expirationTime the time before which users are considered expired
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM User u WHERE u.expiresAt < :expirationTime")
    int deleteExpiredUsers(@Param("expirationTime") LocalDateTime expirationTime);
}
