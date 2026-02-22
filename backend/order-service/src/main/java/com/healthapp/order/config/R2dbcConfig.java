package com.healthapp.order.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * R2DBC repository configuration.
 * Disabled in test profile to avoid requiring database during tests.
 */
@Configuration
@Profile("!test")
@EnableR2dbcRepositories(basePackages = "com.healthapp.order.repository")
public class R2dbcConfig {
}
