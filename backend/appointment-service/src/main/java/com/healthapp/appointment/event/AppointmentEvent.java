package com.healthapp.appointment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentEvent {
    private String eventType;
    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;
    private Instant scheduledAt;
    private String consultationType;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
