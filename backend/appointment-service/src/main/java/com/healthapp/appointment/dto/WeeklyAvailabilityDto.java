package com.healthapp.appointment.dto;

import com.healthapp.appointment.domain.ConsultationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAvailabilityDto {
    private UUID id;
    private UUID doctorId;
    private UUID clinicId;
    private Integer dayOfWeek;
    private String dayName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDurationMinutes;
    private Integer bufferMinutes;
    private ConsultationType consultationType;
    private Integer maxPatientsPerSlot;
    private Boolean isActive;
}
