package com.andev.user.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaSpecificationExecutor;

@NoRepositoryBean
public interface CustomJpaRepository<T, ID extends Serializable>
        extends JpaRepository<T, ID>, EntityGraphJpaRepository<T, ID>, EntityGraphJpaSpecificationExecutor<T> {}
