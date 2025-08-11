package com.andev.user.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.transaction.annotation.Transactional;

import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphSimpleJpaRepository;

import jakarta.persistence.EntityManager;

@Transactional(readOnly = true)
public class CustomJpaRepositoryImpl<T, ID extends Serializable> extends EntityGraphSimpleJpaRepository<T, ID>
        implements CustomJpaRepository<T, ID> {

    private final EntityManager entityManager;
    private final Class<T> domainClass;

    public CustomJpaRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.domainClass = entityInformation.getJavaType();
    }
}
