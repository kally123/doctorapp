package com.healthapp.appointment.dto;

import com.healthapp.appointment.domain.ConsultationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    
    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;
    
    @NotNull(message = "Slot ID is required")
    private UUID slotId;
    
    @NotNull(message = "Consultation type is required")
    private ConsultationType consultationType;
    
    private UUID clinicId;
    private String bookingNotes;
}
