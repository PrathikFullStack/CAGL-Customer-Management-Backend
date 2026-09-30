package com.iexceed.appzillonbanking.cagl.cm.config;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.iexceed.appzillonbanking.cagl.cm.repository.cdh",
    entityManagerFactoryRef = "cdhEntityManagerFactory",
    transactionManagerRef = "cdhTransactionManager"
)
public class CdhDatabaseConfig {

    @Bean(name = "cdhDataSourceProperties")
    @ConfigurationProperties(prefix = "cdh.datasource")
    public DataSourceProperties cdhDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "cdhDataSource")
    @ConfigurationProperties(prefix = "cdh.datasource.hikari")
    public DataSource cdhDataSource(
            @Qualifier("cdhDataSourceProperties") DataSourceProperties properties,
            @Qualifier("primaryDataSource") DataSource primaryDataSource) {
        if (properties.getUrl() == null || properties.getUrl().contains("localhost")) {
            return primaryDataSource;
        }
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "cdhEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean cdhEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("cdhDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("jakarta.persistence.schema-generation.database.action", "update");
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);

        return builder
                .dataSource(dataSource)
                .packages("com.iexceed.appzillonbanking.cagl.cm.entity.cdh")
                .persistenceUnit("cdhPersistenceUnit")
                .properties(properties)
                .build();
    }

    @Bean(name = "cdhTransactionManager")
    public PlatformTransactionManager cdhTransactionManager(
            @Qualifier("cdhEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
