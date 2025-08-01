package com.andev.post.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.andev.post.model.enums.PostStatus;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaSpecificationExecutor;

@NoRepositoryBean
public interface CustomPostJpaRepository<T, ID extends Serializable>
        extends JpaRepository<T, ID>, EntityGraphJpaRepository<T, ID>, EntityGraphJpaSpecificationExecutor<T> {

    List<T> findByStatus(PostStatus postStatus);
}
