package com.healthapp.consultation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Main consultation session entity.
 * Represents a video/audio/chat consultation between patient and doctor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("consultation_sessions")
public class ConsultationSession {
    
    @Id
    private UUID id;
    
    // References
    @Column("appointment_id")
    private UUID appointmentId;
    
    @Column("patient_id")
    private UUID patientId;
    
    @Column("doctor_id")
    private UUID doctorId;
    
    // Session configuration (stored as String for R2DBC)
    @Column("consultation_mode")
    private String consultationMode;
    
    @Column("scheduled_start_time")
    private Instant scheduledStartTime;
    
    @Column("scheduled_duration_minutes")
    private Integer scheduledDurationMinutes;
    
    // Video room details
    @Column("room_name")
    private String roomName;
    
    @Column("room_sid")
    private String roomSid;
    
    // Session tracking (stored as String for R2DBC)
    @Column("status")
    private String status;
    
    // Participant join times
    @Column("patient_joined_at")
    private Instant patientJoinedAt;
    
    @Column("doctor_joined_at")
    private Instant doctorJoinedAt;
    
    // Actual timing
    @Column("actual_start_time")
    private Instant actualStartTime;
    
    @Column("actual_end_time")
    private Instant actualEndTime;
    
    @Column("total_duration_seconds")
    private Integer totalDurationSeconds;
    
    // Quality metrics
    @Column("patient_connection_quality")
    private String patientConnectionQuality;
    
    @Column("doctor_connection_quality")
    private String doctorConnectionQuality;
    
    // Recording
    @Column("is_recorded")
    private Boolean isRecorded;
    
    @Column("recording_url")
    private String recordingUrl;
    
    @Column("recording_duration_seconds")
    private Integer recordingDurationSeconds;
    
    // Metadata
    @Column("end_reason")
    private String endReason;
    
    @Column("notes")
    private String notes;
    
    @CreatedDate
    @Column("created_at")
    private Instant createdAt;
    
    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;
    
    // Helper methods for enum conversion
    public ConsultationMode getConsultationModeEnum() {
        return consultationMode != null ? ConsultationMode.valueOf(consultationMode) : null;
    }
    
    public void setConsultationModeEnum(ConsultationMode mode) {
        this.consultationMode = mode != null ? mode.name() : null;
    }
    
    public SessionStatus getStatusEnum() {
        return status != null ? SessionStatus.valueOf(status) : null;
    }
    
    public void setStatusEnum(SessionStatus sessionStatus) {
        this.status = sessionStatus != null ? sessionStatus.name() : null;
    }
}
