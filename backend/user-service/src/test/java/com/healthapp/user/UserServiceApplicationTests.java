package com.healthapp.user;

import com.healthapp.user.repository.RefreshTokenRepository;
import com.healthapp.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.kafka.sender.KafkaSender;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.r2dbc.enabled=false"
    }
)
@ActiveProfiles("test")
class UserServiceApplicationTests {

    @Configuration
    @EnableAutoConfiguration(exclude = {
        R2dbcDataAutoConfiguration.class,
        R2dbcAutoConfiguration.class,
        R2dbcRepositoriesAutoConfiguration.class,
        FlywayAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class,
        KafkaAutoConfiguration.class
    })
    @ComponentScan(
        basePackages = {"com.healthapp.user", "com.healthapp.common"},
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*R2dbcConfig"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Repository")
        }
    )
    static class TestConfiguration {
    }

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @MockBean
    private KafkaSender<String, Object> kafkaSender;

    @Test
    void contextLoads() {
        // Basic smoke test to ensure application context loads
    }
}
