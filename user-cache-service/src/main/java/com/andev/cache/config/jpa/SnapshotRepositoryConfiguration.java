package com.andev.cache.config.jpa;

import com.andev.cache.config.UserCacheProperties;
import jakarta.persistence.EntityManagerFactory;
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
            @Qualifier("snapshotDataSource") DataSource dataSource,
            UserCacheProperties properties) {

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.andev.cache.model.entities");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "create");
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.show_sql", "false");
        emf.setJpaPropertyMap(props);

        return emf;
    }

    @Bean(name = SNAPSHOT_TRANSACTION_MANAGER)
    @ConditionalOnMissingBean(name = SNAPSHOT_TRANSACTION_MANAGER)
    public PlatformTransactionManager snapshotTransactionManager(
            @Qualifier(SNAPSHOT_ENTITY_MANAGER_FACTORY) EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
