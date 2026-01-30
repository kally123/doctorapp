package com.healthapp.consultation.dto;

import com.healthapp.consultation.domain.ConsultationMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Request to create a new consultation session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {
    
    @NotNull(message = "Appointment ID is required")
    private UUID appointmentId;
    
    @NotNull(message = "Patient ID is required")
    private UUID patientId;
    
    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;
    
    @NotNull(message = "Consultation mode is required")
    private ConsultationMode consultationMode;
    
    @NotNull(message = "Scheduled start time is required")
    @Future(message = "Scheduled start time must be in the future")
    private Instant scheduledStartTime;
    
    @Min(value = 5, message = "Minimum duration is 5 minutes")
    @Max(value = 120, message = "Maximum duration is 120 minutes")
    @Builder.Default
    private Integer scheduledDurationMinutes = 15;
}
