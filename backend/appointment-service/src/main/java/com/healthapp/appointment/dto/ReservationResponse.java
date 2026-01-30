package com.healthapp.appointment.dto;

import com.healthapp.appointment.domain.ConsultationType;
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
public class ReservationResponse {
    private UUID appointmentId;
    private UUID doctorId;
    private Instant scheduledAt;
    private ConsultationType consultationType;
    private BigDecimal consultationFee;
    private BigDecimal platformFee;
    private BigDecimal totalAmount;
    private String currency;
    private Instant reservedUntil;
    private long expiresInSeconds;
}
