package com.andev.post.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.andev.post.model.entities.Post;
import com.andev.post.model.enums.PostStatus;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // =================== BASIC QUERIES ===================

    @Query("SELECT p FROM Post p WHERE p.id IN :ids")
    List<Post> findByIdsIn(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Post p WHERE p.authorId = :authorId")
    Page<Post> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.postStatus = :status")
    Page<Post> findByPostStatus(@Param("status") PostStatus status, Pageable pageable);

    // =================== TRENDING POSTS ===================

    @Query("SELECT p FROM Post p WHERE p.postStatus = 'ACTIVE' "
            + "AND p.created >= :since ORDER BY p.likes DESC, p.created DESC")
    Page<Post> findTrendingPosts(@Param("since") LocalDateTime since, Pageable pageable);

    // =================== OWNERSHIP VALIDATION ===================

    @Query("SELECT COUNT(p) > 0 FROM Post p WHERE p.id = :id AND p.authorId = :authorId")
    boolean existsByIdAndAuthorId(@Param("id") Long id, @Param("authorId") Long authorId);

    // =================== BULK OPERATIONS ===================

    @Modifying
    @Query("UPDATE Post p SET p.postStatus = :status WHERE p.id IN :ids")
    int updatePostStatuses(@Param("ids") List<Long> ids, @Param("status") PostStatus status);

    @Modifying
    @Query("UPDATE Post p SET p.likes = :likes WHERE p.id = :id")
    int updateLikes(@Param("id") Long id, @Param("likes") Integer likes);

    @Modifying
    @Query("UPDATE Post p SET p.commentsIds = :commentIds WHERE p.id = :id")
    int updateCommentIds(@Param("id") Long id, @Param("commentIds") Set<Long> commentIds);

    // =================== STATUS SPECIFIC QUERIES ===================

    @Query("SELECT p FROM Post p WHERE p.postStatus = :status AND p.authorId = :authorId")
    Page<Post> findByPostStatusAndAuthorId(
            @Param("status") PostStatus status, @Param("authorId") Long authorId, Pageable pageable);
}
