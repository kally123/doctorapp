package com.healthapp.appointment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockSlotRequest {
    
    @NotNull(message = "Start datetime is required")
    private Instant startDatetime;
    
    @NotNull(message = "End datetime is required")
    private Instant endDatetime;
    
    private String reason;
    private String blockType;
    private Boolean isRecurring;
    private String recurrencePattern;
}
