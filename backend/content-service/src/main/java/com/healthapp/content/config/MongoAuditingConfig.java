package com.healthapp.content.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/**
 * MongoDB auditing configuration.
 * Disabled in test profile to avoid requiring MongoDB during tests.
 */
@Configuration
@Profile("!test")
@EnableReactiveMongoAuditing
public class MongoAuditingConfig {
}
