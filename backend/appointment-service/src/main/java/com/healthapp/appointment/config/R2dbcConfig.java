package com.healthapp.appointment.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.healthapp.appointment.repository")
@EnableR2dbcAuditing
@ConditionalOnProperty(name = "spring.r2dbc.enabled", havingValue = "true", matchIfMissing = true)
public class R2dbcConfig {
    
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
    }
}
