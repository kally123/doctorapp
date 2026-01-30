package com.healthapp.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotsResponse {
    private UUID doctorId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DaySlots> days;
    private int totalAvailableSlots;
}
