package com.andev.comment.config.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.andev.comment.config.model.entitie.Comments;

@Repository
public interface CommentRepository extends MongoRepository<Comments, String> {

    @Query("{'postId': ?0, 'parentCommentId': null, 'isDeleted': false}")
    List<Comments> findByPostIdOrderByCreatedAtDesc(String postId);

    @Query("{'postId': ?0, 'isDeleted': false}")
    List<Comments> findAllByPostId(String postId);

    @Query("{'_id': ?0}")
    Optional<Comments> findByStringId(String id);
}
