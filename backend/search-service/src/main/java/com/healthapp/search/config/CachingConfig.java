package com.healthapp.search.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Redis caching configuration.
 * Disabled in test profile to avoid requiring Redis during tests.
 */
@Configuration
@Profile("!test")
@EnableCaching
public class CachingConfig {
}
