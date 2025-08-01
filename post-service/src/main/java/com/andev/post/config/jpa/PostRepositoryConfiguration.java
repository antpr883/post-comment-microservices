package com.andev.post.config.jpa;

import static com.andev.post.config.jpa.TransactionConstants.POST_ENTITY_MANAGER_FACTORY;
import static com.andev.post.config.jpa.TransactionConstants.POST_TRANSACTION_MANAGER;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.andev.post.repository.CustomPostPostJpaRepositoryImpl;
import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@ConditionalOnClass(PostJpaRepositories.class)
@EnableJpaRepositories(
        basePackages = "com.andev.post.repository",
        entityManagerFactoryRef = POST_ENTITY_MANAGER_FACTORY,
        transactionManagerRef = POST_TRANSACTION_MANAGER,
        repositoryBaseClass = CustomPostPostJpaRepositoryImpl.class,
        repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean.class)
public class PostRepositoryConfiguration {

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/post_hub?ssl=false}")
    private String dataSourceUrl;

    @Value("${spring.datasource.username:post_user}")
    private String dataSourceUsername;

    @Value("${spring.datasource.password:1234}")
    private String dataSourcePassword;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String dataSourceDriverClassName;

    @Primary
    @Bean
    public DataSource postDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(dataSourceUrl)
                .username(dataSourceUsername)
                .password(dataSourcePassword)
                .driverClassName(dataSourceDriverClassName)
                .build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean postEntityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("postDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource)
                .packages("com.andev.post.model.entities")
                .persistenceUnit("primary")
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager postTransactionManager(
            @Qualifier(POST_ENTITY_MANAGER_FACTORY) EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
