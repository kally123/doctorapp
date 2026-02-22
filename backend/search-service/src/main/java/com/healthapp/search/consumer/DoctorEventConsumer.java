package com.healthapp.search.consumer;

import com.healthapp.common.event.BaseEvent;
import com.healthapp.common.event.KafkaTopics;
import com.healthapp.search.model.DoctorDocument;
import com.healthapp.search.service.DoctorSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Kafka consumer for doctor events to keep search index in sync.
 * Disabled in test profile to avoid requiring Kafka and Elasticsearch during tests.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DoctorEventConsumer {
    
    private final DoctorSearchService searchService;
    
    @KafkaListener(topics = KafkaTopics.DOCTOR_EVENTS, groupId = "search-service")
    public void handleDoctorEvent(BaseEvent event) {
        log.info("Received doctor event: {} for {}", event.getEventType(), event.getAggregateId());
        
        try {
            switch (event.getEventType()) {
                case "DOCTOR_CREATED":
                    handleDoctorCreated(event);
                    break;
                case "DOCTOR_UPDATED":
                    handleDoctorUpdated(event);
                    break;
                case "DOCTOR_VERIFIED":
                    handleDoctorVerified(event);
                    break;
                case "DOCTOR_AVAILABILITY_CHANGED":
                    handleDoctorAvailabilityChanged(event);
                    break;
                case "DOCTOR_DELETED":
                    handleDoctorDeleted(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing doctor event: {}", event.getEventType(), e);
            // In production, would send to dead letter queue
        }
    }
    
    private void handleDoctorCreated(BaseEvent event) {
        Map<String, Object> payload = event.getPayload();
        
        DoctorDocument document = DoctorDocument.builder()
                .id(getString(payload, "doctorId"))
                .userId(getString(payload, "userId"))
                .fullName(getString(payload, "name"))
                .isVerified(getBoolean(payload, "isVerified"))
                .isAcceptingPatients(getBoolean(payload, "isAcceptingPatients"))
                .rating(getDouble(payload, "rating"))
                .consultationFee(getBigDecimal(payload, "consultationFee"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        searchService.indexDoctor(document)
                .doOnSuccess(doc -> log.info("Indexed new doctor: {}", doc.getId()))
                .doOnError(e -> log.error("Failed to index doctor: {}", event.getAggregateId(), e))
                .subscribe();
    }
    
    private void handleDoctorUpdated(BaseEvent event) {
        Map<String, Object> payload = event.getPayload();
        String doctorId = getString(payload, "doctorId");
        
        // In a real implementation, we'd fetch the full doctor data from doctor-service
        // For now, we'll update with the available data
        DoctorDocument document = DoctorDocument.builder()
                .id(doctorId)
                .userId(getString(payload, "userId"))
                .fullName(getString(payload, "name"))
                .isVerified(getBoolean(payload, "isVerified"))
                .isAcceptingPatients(getBoolean(payload, "isAcceptingPatients"))
                .rating(getDouble(payload, "rating"))
                .consultationFee(getBigDecimal(payload, "consultationFee"))
                .updatedAt(Instant.now())
                .build();
        
        searchService.updateDoctor(document)
                .doOnSuccess(doc -> log.info("Updated doctor index: {}", doc.getId()))
                .doOnError(e -> log.error("Failed to update doctor index: {}", doctorId, e))
                .subscribe();
    }
    
    private void handleDoctorVerified(BaseEvent event) {
        Map<String, Object> payload = event.getPayload();
        String doctorId = getString(payload, "doctorId");
        
        DoctorDocument document = DoctorDocument.builder()
                .id(doctorId)
                .userId(getString(payload, "userId"))
                .fullName(getString(payload, "name"))
                .isVerified(true)
                .isAcceptingPatients(getBoolean(payload, "isAcceptingPatients"))
                .rating(getDouble(payload, "rating"))
                .consultationFee(getBigDecimal(payload, "consultationFee"))
                .updatedAt(Instant.now())
                .build();
        
        searchService.updateDoctor(document)
                .doOnSuccess(doc -> log.info("Updated doctor verification status: {}", doc.getId()))
                .subscribe();
    }
    
    private void handleDoctorAvailabilityChanged(BaseEvent event) {
        Map<String, Object> payload = event.getPayload();
        String doctorId = getString(payload, "doctorId");
        
        DoctorDocument document = DoctorDocument.builder()
                .id(doctorId)
                .userId(getString(payload, "userId"))
                .fullName(getString(payload, "name"))
                .isVerified(getBoolean(payload, "isVerified"))
                .isAcceptingPatients(getBoolean(payload, "isAcceptingPatients"))
                .rating(getDouble(payload, "rating"))
                .consultationFee(getBigDecimal(payload, "consultationFee"))
                .updatedAt(Instant.now())
                .build();
        
        searchService.updateDoctor(document)
                .doOnSuccess(doc -> log.info("Updated doctor availability: {}", doc.getId()))
                .subscribe();
    }
    
    private void handleDoctorDeleted(BaseEvent event) {
        String doctorId = event.getAggregateId();
        
        searchService.deleteDoctor(doctorId)
                .doOnSuccess(v -> log.info("Deleted doctor from index: {}", doctorId))
                .doOnError(e -> log.error("Failed to delete doctor from index: {}", doctorId, e))
                .subscribe();
    }
    
    // Helper methods for type-safe payload extraction
    
    private String getString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value != null ? value.toString() : null;
    }
    
    private Boolean getBoolean(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Boolean) return (Boolean) value;
        if (value != null) return Boolean.parseBoolean(value.toString());
        return null;
    }
    
    private Double getDouble(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value != null) return Double.parseDouble(value.toString());
        return null;
    }
    
    private BigDecimal getBigDecimal(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        if (value != null) return new BigDecimal(value.toString());
        return null;
    }
}
