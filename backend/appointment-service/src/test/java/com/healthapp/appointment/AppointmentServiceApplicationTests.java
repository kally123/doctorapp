package com.healthapp.appointment;

import com.healthapp.appointment.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.kafka.sender.KafkaSender;

@SpringBootTest(
    classes = AppointmentServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration," +
            "org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration," +
            "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration," +
            "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "spring.r2dbc.enabled=false"
    }
)
@ActiveProfiles("test")
class AppointmentServiceApplicationTests {

    @MockBean
    private AppointmentRepository appointmentRepository;

    @MockBean
    private AppointmentStatusHistoryRepository appointmentStatusHistoryRepository;

    @MockBean
    private AvailableSlotRepository availableSlotRepository;

    @MockBean
    private BlockedSlotRepository blockedSlotRepository;

    @MockBean
    private WeeklyAvailabilityRepository weeklyAvailabilityRepository;

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
