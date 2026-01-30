package com.healthapp.appointment.dto;

import com.healthapp.appointment.domain.AppointmentStatus;
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
public class AppointmentDto {
    private UUID id;
    private UUID patientId;
    private UUID doctorId;
    private UUID clinicId;
    
    private Instant scheduledAt;
    private Integer durationMinutes;
    
    private ConsultationType consultationType;
    private AppointmentStatus status;
    
    private BigDecimal consultationFee;
    private BigDecimal platformFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
    
    private String paymentStatus;
    private String bookingNotes;
    
    private Instant cancelledAt;
    private String cancellationReason;
    private BigDecimal refundAmount;
    private String refundStatus;
    
    private Boolean isFollowup;
    private Integer rescheduleCount;
    
    private Instant createdAt;
    
    // Additional display fields (populated from other services)
    private String doctorName;
    private String patientName;
    private String clinicName;
    private String clinicAddress;
}
