package com.healthapp.doctor.event;

import com.healthapp.common.event.BaseEvent;
import com.healthapp.common.event.KafkaTopics;
import com.healthapp.doctor.model.entity.Doctor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Publishes doctor-related events to Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorEventPublisher {
    
    private final ReactiveKafkaProducerTemplate<String, Object> kafkaProducerTemplate;
    
    /**
     * Publishes doctor profile created event.
     */
    public Mono<Void> publishDoctorCreated(Doctor doctor) {
        return publishEvent(
                KafkaTopics.DOCTOR_EVENTS,
                doctor.getId().toString(),
                createEvent("DOCTOR_CREATED", doctor)
        );
    }
    
    /**
     * Publishes doctor profile updated event.
     */
    public Mono<Void> publishDoctorUpdated(Doctor doctor) {
        return publishEvent(
                KafkaTopics.DOCTOR_EVENTS,
                doctor.getId().toString(),
                createEvent("DOCTOR_UPDATED", doctor)
        );
    }
    
    /**
     * Publishes doctor verified event.
     */
    public Mono<Void> publishDoctorVerified(Doctor doctor) {
        return publishEvent(
                KafkaTopics.DOCTOR_EVENTS,
                doctor.getId().toString(),
                createEvent("DOCTOR_VERIFIED", doctor)
        );
    }
    
    /**
     * Publishes doctor availability changed event.
     */
    public Mono<Void> publishDoctorAvailabilityChanged(Doctor doctor) {
        return publishEvent(
                KafkaTopics.DOCTOR_EVENTS,
                doctor.getId().toString(),
                createEvent("DOCTOR_AVAILABILITY_CHANGED", doctor)
        );
    }
    
    private BaseEvent createEvent(String eventType, Doctor doctor) {
        return BaseEvent.builder()
                .eventType(eventType)
                .correlationId(UUID.randomUUID().toString())
                .metadata(Map.of(
                        "aggregateId", doctor.getId().toString(),
                        "aggregateType", "Doctor"
                ))
                .data(Map.of(
                        "doctorId", doctor.getId().toString(),
                        "userId", doctor.getUserId().toString(),
                        "name", doctor.getFullName(),
                        "isVerified", doctor.getIsVerified(),
                        "isAcceptingPatients", doctor.getIsAcceptingPatients(),
                        "rating", doctor.getRating(),
                        "consultationFee", doctor.getConsultationFee() != null ? doctor.getConsultationFee() : BigDecimal.ZERO
                ))
                .build();
    }
    
    private Mono<Void> publishEvent(String topic, String key, BaseEvent event) {
        return kafkaProducerTemplate.send(topic, key, event)
                .doOnSuccess(result -> log.info(
                        "Published event {} to topic {} with key {}",
                        event.getEventType(), topic, key
                ))
                .doOnError(error -> log.error(
                        "Failed to publish event {} to topic {}",
                        event.getEventType(), topic, error
                ))
                .then();
    }
}
