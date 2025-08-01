package com.andev.cache.config.jpa;

import com.andev.cache.config.UserCacheProperties;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static com.andev.cache.config.jpa.TransactionSnapshotConstants.SNAPSHOT_ENTITY_MANAGER_FACTORY;
import static com.andev.cache.config.jpa.TransactionSnapshotConstants.SNAPSHOT_TRANSACTION_MANAGER;


@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.andev.cache.repository",
        entityManagerFactoryRef = SNAPSHOT_ENTITY_MANAGER_FACTORY,
        transactionManagerRef = SNAPSHOT_TRANSACTION_MANAGER,
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.postservice\\..*"))
@ConditionalOnProperty(name = "user-cache.enabled", havingValue = "true", matchIfMissing = true)
public class SnapshotRepositoryConfiguration {

    @Bean(name = SNAPSHOT_ENTITY_MANAGER_FACTORY)
    @ConditionalOnMissingBean(name = SNAPSHOT_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean snapshotEntityManagerFactory(
            @Qualifier("snapshotDataSource") DataSource dataSource) {

        log.info("=== CREATING SNAPSHOT ENTITY MANAGER FACTORY ===");
        log.info("Packages to scan: com.andev.cache.model.entities");
        log.info("DataSource: {}", dataSource.getClass().getSimpleName());

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.andev.cache.model.entities");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "create");
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.default_schema", "v1_snap_user");
        emf.setJpaPropertyMap(props);

        log.info("Hibernate properties: {}", props);
        log.info("=== SNAPSHOT ENTITY MANAGER FACTORY CREATED ===");

        return emf;
    }

    @Bean(name = SNAPSHOT_TRANSACTION_MANAGER)
    @ConditionalOnMissingBean(name = SNAPSHOT_TRANSACTION_MANAGER)
    public PlatformTransactionManager snapshotTransactionManager(
            @Qualifier(SNAPSHOT_ENTITY_MANAGER_FACTORY) EntityManagerFactory emf) {
        log.info("=== CREATING SNAPSHOT TRANSACTION MANAGER ===");
        JpaTransactionManager transactionManager = new JpaTransactionManager(emf);
        log.info("=== SNAPSHOT TRANSACTION MANAGER CREATED ===");
        return transactionManager;
    }
}
