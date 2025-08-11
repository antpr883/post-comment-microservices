package com.andev.user.config.jpa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.andev.user.UserServiceApplication;
import com.andev.user.repository.CustomJpaRepositoryImpl;
import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean;

@EnableJpaAuditing
@Configuration
@ConditionalOnClass(CustomJpaRepositories.class)
@Import(BaseRepositoryAutoConfiguration.JpaConfiguration.class)
public class BaseRepositoryAutoConfiguration {

    @Configuration
    @EntityScan(basePackageClasses = UserServiceApplication.class)
    @EnableJpaRepositories(
            basePackageClasses = UserServiceApplication.class,
            repositoryBaseClass = CustomJpaRepositoryImpl.class,
            repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean.class)
    public class JpaConfiguration {}
}
