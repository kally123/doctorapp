package com.healthapp.consultation.event;

import org.springframework.context.annotation.Profile;
import com.healthapp.consultation.domain.ConsultationSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Publishes consultation events to Kafka.
 */
@Slf4j
@Profile("!test")
@Component
@RequiredArgsConstructor
public class ConsultationEventPublisher {
    
    private final KafkaTemplate<String, ConsultationEvent> kafkaTemplate;
    
    @Value("${kafka.topics.consultation-events:consultation-events}")
    private String consultationEventsTopic;
    
    public void publishSessionCreated(ConsultationSession session) {
        publishEvent(session, ConsultationEvent.EventType.SESSION_CREATED);
    }
    
    public void publishSessionStarted(ConsultationSession session) {
        publishEvent(session, ConsultationEvent.EventType.SESSION_STARTED);
    }
    
    public void publishSessionEnded(ConsultationSession session) {
        publishEvent(session, ConsultationEvent.EventType.SESSION_ENDED);
    }
    
    public void publishParticipantJoined(ConsultationSession session, UUID userId, String participantType) {
        ConsultationEvent event = ConsultationEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(ConsultationEvent.EventType.PARTICIPANT_JOINED)
                .sessionId(session.getId())
                .appointmentId(session.getAppointmentId())
                .patientId(session.getPatientId())
                .doctorId(session.getDoctorId())
                .roomName(session.getRoomName())
                .participantUserId(userId)
                .participantType(participantType)
                .timestamp(Instant.now())
                .build();
        
        sendEvent(event);
    }
    
    public void publishParticipantLeft(ConsultationSession session, UUID userId, String participantType) {
        ConsultationEvent event = ConsultationEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(ConsultationEvent.EventType.PARTICIPANT_LEFT)
                .sessionId(session.getId())
                .appointmentId(session.getAppointmentId())
                .patientId(session.getPatientId())
                .doctorId(session.getDoctorId())
                .roomName(session.getRoomName())
                .participantUserId(userId)
                .participantType(participantType)
                .timestamp(Instant.now())
                .build();
        
        sendEvent(event);
    }
    
    private void publishEvent(ConsultationSession session, ConsultationEvent.EventType eventType) {
        ConsultationEvent event = ConsultationEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .sessionId(session.getId())
                .appointmentId(session.getAppointmentId())
                .patientId(session.getPatientId())
                .doctorId(session.getDoctorId())
                .roomName(session.getRoomName())
                .consultationMode(session.getConsultationMode())
                .status(session.getStatus())
                .scheduledStartTime(session.getScheduledStartTime())
                .actualStartTime(session.getActualStartTime())
                .actualEndTime(session.getActualEndTime())
                .totalDurationSeconds(session.getTotalDurationSeconds())
                .endReason(session.getEndReason())
                .timestamp(Instant.now())
                .build();
        
        sendEvent(event);
    }
    
    private void sendEvent(ConsultationEvent event) {
        try {
            kafkaTemplate.send(consultationEventsTopic, event.getSessionId().toString(), event);
            log.info("Published {} event for session: {}", event.getEventType(), event.getSessionId());
        } catch (Exception e) {
            log.error("Failed to publish event: {} for session: {}", event.getEventType(), event.getSessionId(), e);
        }
    }
}
