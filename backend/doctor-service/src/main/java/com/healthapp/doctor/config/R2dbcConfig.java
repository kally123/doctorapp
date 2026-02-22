package com.healthapp.doctor.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * R2DBC database configuration for reactive PostgreSQL access.
 */
@Configuration
@EnableR2dbcAuditing
@EnableR2dbcRepositories(basePackages = "com.healthapp.doctor.repository")
@EnableTransactionManagement
@ConditionalOnProperty(name = "spring.r2dbc.enabled", havingValue = "true", matchIfMissing = true)
public class R2dbcConfig {
    
    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}
