package com.andev.post.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

import com.andev.post.model.enums.PostStatus;
import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphSimpleJpaRepository;

import jakarta.persistence.EntityManager;

public class CustomPostPostJpaRepositoryImpl<T, ID extends Serializable> extends EntityGraphSimpleJpaRepository<T, ID>
        implements CustomPostJpaRepository<T, ID> {

    private final EntityManager entityManager;
    private final Class<T> domainClass;

    public CustomPostPostJpaRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.domainClass = entityInformation.getJavaType();
    }

    @Override
    public List<T> findByStatus(PostStatus status) {
        Specification<T> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("postStatus"), status);
        return findAll(spec);
    }
}
