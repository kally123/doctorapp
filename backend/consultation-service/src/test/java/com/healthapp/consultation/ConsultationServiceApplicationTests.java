package com.healthapp.consultation;

import com.healthapp.consultation.event.ConsultationEventPublisher;
import com.healthapp.consultation.repository.*;
import com.healthapp.consultation.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.kafka.sender.KafkaSender;

@SpringBootTest(
    classes = ConsultationServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration," +
            "org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration," +
            "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration," +
            "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
    }
)
@ActiveProfiles("test")
class ConsultationServiceApplicationTests {

    @MockBean
    private ConsultationSessionRepository consultationSessionRepository;

    @MockBean
    private SessionParticipantRepository sessionParticipantRepository;

    @MockBean
    private SessionEventRepository sessionEventRepository;

    @MockBean
    private ChatMessageRepository chatMessageRepository;

    @MockBean
    private ConsultationFeedbackRepository consultationFeedbackRepository;

    @MockBean
    private ConsultationPricingRepository consultationPricingRepository;

    // Mock Services (they have @Profile("!test") so won't be created automatically)
    @MockBean
    private ChatService chatService;

    @MockBean
    private ConsultationSessionService consultationSessionService;

    @MockBean
    private FeedbackService feedbackService;

    @MockBean
    private TwilioVideoService twilioVideoService;

    @MockBean
    private ConsultationEventPublisher eventPublisher;

    // Mock Infrastructure
    @MockBean
    private SimpMessagingTemplate messagingTemplate;

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
