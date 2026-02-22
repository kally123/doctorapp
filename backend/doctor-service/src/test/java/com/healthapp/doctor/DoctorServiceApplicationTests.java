package com.healthapp.doctor;

import com.healthapp.doctor.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.kafka.sender.KafkaSender;

@SpringBootTest(
    classes = DoctorServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration," +
            "org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration," +
            "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration," +
            "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "spring.r2dbc.enabled=false",
        "spring.cache.redis.enabled=false"
    }
)
@ActiveProfiles("test")
class DoctorServiceApplicationTests {

    @MockBean
    private DoctorRepository doctorRepository;

    @MockBean
    private SpecializationRepository specializationRepository;

    @MockBean
    private QualificationRepository qualificationRepository;

    @MockBean
    private LanguageRepository languageRepository;

    @MockBean
    private ClinicRepository clinicRepository;

    @MockBean
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @MockBean
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @MockBean
    private KafkaSender<String, Object> kafkaSender;

    @Test
    void contextLoads() {
        // Basic smoke test to ensure application context loads
    }
}
