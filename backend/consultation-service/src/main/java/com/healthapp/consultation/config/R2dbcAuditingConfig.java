package com.healthapp.consultation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

/**
 * R2DBC auditing configuration.
 * Disabled in test profile to avoid requiring database during tests.
 */
@Configuration
@Profile("!test")
@EnableR2dbcAuditing
public class R2dbcAuditingConfig {
}
