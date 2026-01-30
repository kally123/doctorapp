package com.healthapp.consultation.dto;

import com.healthapp.consultation.domain.ConsultationMode;
import com.healthapp.consultation.domain.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response containing consultation session details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    
    private UUID id;
    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;
    
    private ConsultationMode consultationMode;
    private SessionStatus status;
    
    private Instant scheduledStartTime;
    private Integer scheduledDurationMinutes;
    
    private String roomName;
    
    private Instant patientJoinedAt;
    private Instant doctorJoinedAt;
    private Instant actualStartTime;
    private Instant actualEndTime;
    private Integer totalDurationSeconds;
    
    private String patientConnectionQuality;
    private String doctorConnectionQuality;
    
    private Boolean isRecorded;
    private String recordingUrl;
    
    private String endReason;
    private String notes;
    
    private Instant createdAt;
    private Instant updatedAt;
}
