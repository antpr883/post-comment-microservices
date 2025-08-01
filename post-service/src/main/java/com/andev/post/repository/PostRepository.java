package com.andev.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.andev.post.model.entities.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // Basic JPA repository without custom configuration
}
