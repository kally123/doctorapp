package com.healthapp.consultation.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published to Kafka for consultation lifecycle events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationEvent {
    
    private UUID eventId;
    private EventType eventType;
    private UUID sessionId;
    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;
    private String roomName;
    private String consultationMode;
    private String status;
    private Instant scheduledStartTime;
    private Instant actualStartTime;
    private Instant actualEndTime;
    private Integer totalDurationSeconds;
    private String endReason;
    
    // For participant events
    private UUID participantUserId;
    private String participantType;
    
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    public enum EventType {
        SESSION_CREATED,
        SESSION_STARTED,
        SESSION_ENDED,
        SESSION_CANCELLED,
        PARTICIPANT_JOINED,
        PARTICIPANT_LEFT,
        RECORDING_STARTED,
        RECORDING_STOPPED
    }
}
