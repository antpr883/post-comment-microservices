package com.andev.cache.config.jpa;


import com.andev.cache.config.CacheServiceProperties;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
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
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

import static com.andev.cache.config.jpa.TransactionSnapshotConstants.SNAPSHOT_ENTITY_MANAGER_FACTORY;
import static com.andev.cache.config.jpa.TransactionSnapshotConstants.SNAPSHOT_TRANSACTION_MANAGER;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.andev.cache.repository.snapshot",
        entityManagerFactoryRef = SNAPSHOT_ENTITY_MANAGER_FACTORY,
        transactionManagerRef = "snapshotTransactionManager",
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.postservice\\..*"))
@ConditionalOnProperty(name = "user-cache.enabled", havingValue = "true", matchIfMissing = true)
public class SnapshotRepositoryConfiguration {

    @Bean(name = SNAPSHOT_ENTITY_MANAGER_FACTORY)
    @ConditionalOnMissingBean(name = SNAPSHOT_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean snapshotEntityManagerFactory(
            @Qualifier("snapshotDataSource") DataSource dataSource) {

        log.info("🔧 SNAPSHOT EMF: Creating EntityManagerFactory for snapshot datasource");
        log.info("🔧 SNAPSHOT EMF: DataSource class: {}", dataSource.getClass().getSimpleName());
        
        try {
            log.info("🔧 SNAPSHOT EMF: DataSource URL: {}", dataSource.getConnection().getMetaData().getURL());
        } catch (Exception e) {
            log.warn("🔧 SNAPSHOT EMF: Could not get DataSource URL: {}", e.getMessage());
        }

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.andev.cache.model.entities");
        emf.setPersistenceUnitName("snapshot");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(true);
        vendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");
        emf.setJpaVendorAdapter(vendorAdapter);

        log.info("🔧 SNAPSHOT EMF: EntityManagerFactory created with SQL logging enabled");
        return emf;
    }

    @Bean(name = SNAPSHOT_TRANSACTION_MANAGER)
    @ConditionalOnMissingBean(name = SNAPSHOT_TRANSACTION_MANAGER)
    public PlatformTransactionManager snapshotTransactionManager(
            @Qualifier(SNAPSHOT_ENTITY_MANAGER_FACTORY) EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
