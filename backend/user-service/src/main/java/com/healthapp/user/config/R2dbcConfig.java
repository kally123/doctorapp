package com.healthapp.user.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

/**
 * R2DBC configuration for reactive database access.
 */
@Configuration
@EnableR2dbcAuditing
@ConditionalOnProperty(name = "spring.r2dbc.enabled", havingValue = "true", matchIfMissing = true)
public class R2dbcConfig {
    // Additional R2DBC configuration can be added here
}
