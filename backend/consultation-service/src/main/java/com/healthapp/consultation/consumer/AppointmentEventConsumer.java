package com.healthapp.consultation.consumer;

import com.healthapp.consultation.domain.ConsultationMode;
import com.healthapp.consultation.dto.CreateSessionRequest;
import com.healthapp.consultation.service.ConsultationSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Consumes appointment events to auto-create consultation sessions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentEventConsumer {
    
    private final ConsultationSessionService sessionService;
    
    @KafkaListener(topics = "${kafka.topics.appointment-events:appointment-events}", 
                   groupId = "${spring.kafka.consumer.group-id}")
    public void handleAppointmentEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            log.info("Received appointment event: {}", eventType);
            
            switch (eventType) {
                case "CONFIRMED" -> handleAppointmentConfirmed(event);
                case "CANCELLED" -> handleAppointmentCancelled(event);
                case "RESCHEDULED" -> handleAppointmentRescheduled(event);
                default -> log.debug("Ignoring event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing appointment event: {}", e.getMessage(), e);
        }
    }
    
    private void handleAppointmentConfirmed(Map<String, Object> event) {
        UUID appointmentId = UUID.fromString((String) event.get("appointmentId"));
        UUID patientId = UUID.fromString((String) event.get("patientId"));
        UUID doctorId = UUID.fromString((String) event.get("doctorId"));
        String consultationType = (String) event.getOrDefault("consultationType", "VIDEO");
        Instant scheduledTime = Instant.parse((String) event.get("scheduledTime"));
        Integer duration = (Integer) event.getOrDefault("durationMinutes", 15);
        
        // Only create session for teleconsultation appointments
        if ("VIDEO".equals(consultationType) || "AUDIO".equals(consultationType)) {
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .appointmentId(appointmentId)
                    .patientId(patientId)
                    .doctorId(doctorId)
                    .consultationMode(ConsultationMode.valueOf(consultationType))
                    .scheduledStartTime(scheduledTime)
                    .scheduledDurationMinutes(duration)
                    .build();
            
            sessionService.createSession(request)
                    .subscribe(
                            session -> log.info("Created consultation session {} for appointment {}", 
                                    session.getId(), appointmentId),
                            error -> log.error("Failed to create session for appointment {}: {}", 
                                    appointmentId, error.getMessage())
                    );
        } else {
            log.debug("Skipping session creation for in-person appointment: {}", appointmentId);
        }
    }
    
    private void handleAppointmentCancelled(Map<String, Object> event) {
        UUID appointmentId = UUID.fromString((String) event.get("appointmentId"));
        
        sessionService.getSessionByAppointment(appointmentId)
                .flatMap(session -> sessionService.endSession(session.getId(), "appointment_cancelled"))
                .subscribe(
                        session -> log.info("Cancelled consultation session for appointment: {}", appointmentId),
                        error -> log.warn("No session found for cancelled appointment: {}", appointmentId)
                );
    }
    
    private void handleAppointmentRescheduled(Map<String, Object> event) {
        UUID appointmentId = UUID.fromString((String) event.get("appointmentId"));
        Instant newScheduledTime = Instant.parse((String) event.get("scheduledTime"));
        
        // For rescheduled appointments, we may need to update the session
        // This is a simplified implementation - in production, you might cancel and recreate
        sessionService.getSessionByAppointment(appointmentId)
                .subscribe(
                        session -> log.info("Session found for rescheduled appointment: {} - may need manual update", 
                                appointmentId),
                        error -> log.debug("No existing session for rescheduled appointment: {}", appointmentId)
                );
    }
}
