package com.healthapp.appointment.dto;

import com.healthapp.appointment.domain.ConsultationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAvailabilityRequest {
    
    @NotNull(message = "Day of week is required")
    @Min(0) @Max(6)
    private Integer dayOfWeek;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    @NotNull(message = "Slot duration is required")
    @Min(5) @Max(120)
    private Integer slotDurationMinutes;
    
    @Min(0) @Max(60)
    private Integer bufferMinutes;
    
    @NotNull(message = "Consultation type is required")
    private ConsultationType consultationType;
    
    private String clinicId;
}
