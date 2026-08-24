package com.iexceed.appzillonbanking.cagl.loan;

import javax.sql.DataSource;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.iexceed.appzillonbanking.core.utils.JasyptConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    entityManagerFactoryRef = "abEntityManagerFactory",
    transactionManagerRef = "abTransactionManager",
    basePackages = {
        "com.iexceed.appzillonbanking.cagl.repository.ab",
        "com.iexceed.appzillonbanking.cagl.*.repository.ab",
        "com.iexceed.appzillonbanking.cagl.loan.bulkupload.repository.ab",
        "com.iexceed.appzillonbanking.*.repository.ab",
        "com.iexceed.appzillonbanking.cagl.loan.bulkupload.domain.ab",
        "com.iexceed.appzillonbanking.cagl.repository.cus"
    }
)
public class AbDataConfiguration {

    // --- JDBC basics from spring.datasource.* ---
    @Primary
    @Bean(name = "abDataSourceProps")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    // --- ALL Hikari props from spring.datasource.hikari.* ---
    @Primary
    @Bean(name = "abHikariConfig")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    // --- Bind JPA props from spring.jpa.* (show-sql, ddl-auto, and spring.jpa.properties.* map) ---
    @Primary
    @Bean(name = "abJpaProperties")
    @ConfigurationProperties(prefix = "spring.jpa")
    public JpaProperties jpaProperties() {
        return new JpaProperties();
    }

    // --- Build DataSource with decrypted password and Hikari config ---
    @Primary
    @Bean(name = "abDataSource")
    public DataSource dataSource(
            @Qualifier("abDataSourceProps") DataSourceProperties props,
            @Qualifier("abHikariConfig") HikariConfig cfg) {

        if (props.getUrl() != null) cfg.setJdbcUrl(props.getUrl());
        if (props.getUsername() != null) cfg.setUsername(props.getUsername());

        String password = props.getPassword();
        if (password != null) {
            JasyptConfig jasypt = new JasyptConfig();
            String decrypted = jasypt.getPasswordEncryptor().decrypt(password);
            cfg.setPassword(decrypted);
        }
        if (props.getDriverClassName() != null) {
            cfg.setDriverClassName(props.getDriverClassName());
        }

        return new HikariDataSource(cfg);
    }

    // --- EntityManagerFactory that forwards all JPA/Hibernate properties ---
    @Primary
    @Bean(name = "abEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean abEntityManagerFactory(
            org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder builder,
            @Qualifier("abDataSource") DataSource abDataSource,
            @Qualifier("abJpaProperties") JpaProperties jpaProps) {

        // Spring merges:
        //  - spring.jpa.show-sql, ddl-auto, etc.
        //  - spring.jpa.properties.* (e.g., hibernate.format_sql, dialect, naming strategy)

        return builder
            .dataSource(abDataSource)
            .packages(
                "com.iexceed.appzillonbanking.*.domain.ab",
                "com.iexceed.appzillonbanking.cagl.domain.ab",
                "com.iexceed.appzillonbanking.cagl.*.domain.ab",
                "com.iexceed.appzillonbanking.cagl.loan.bulkupload.repository.ab",
                "com.iexceed.appzillonbanking.cagl.*.domain.ab",
                "com.iexceed.appzillonbanking.cagl.domain.cus",
                "com.iexceed.appzillonbanking.cagl.loan.bulkupload.domain.ab",
                "com.iexceed.appzillonbanking.cagl.entity"
            )
            .persistenceUnit("abdata")
            .properties(jpaProps.getProperties())   // <-- fixed
            .build();
    }

    // --- Transaction manager ---
    @Primary
    @Bean(name = "abTransactionManager")
    public PlatformTransactionManager abTransactionManager(
            @Qualifier("abEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
