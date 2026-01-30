package com.healthapp.appointment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing an appointment booking
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("appointments")
public class Appointment {
    
    @Id
    private UUID id;
    
    @Column("patient_id")
    private UUID patientId;
    
    @Column("doctor_id")
    private UUID doctorId;
    
    @Column("clinic_id")
    private UUID clinicId;
    
    @Column("scheduled_at")
    private Instant scheduledAt;
    
    @Column("duration_minutes")
    private Integer durationMinutes;
    
    @Column("consultation_type")
    private ConsultationType consultationType;
    
    @Column("status")
    private AppointmentStatus status;
    
    @Column("slot_id")
    private UUID slotId;
    
    @Column("consultation_fee")
    private BigDecimal consultationFee;
    
    @Column("platform_fee")
    private BigDecimal platformFee;
    
    @Column("discount_amount")
    private BigDecimal discountAmount;
    
    @Column("total_amount")
    private BigDecimal totalAmount;
    
    @Column("currency")
    private String currency;
    
    @Column("payment_id")
    private UUID paymentId;
    
    @Column("payment_status")
    private String paymentStatus;
    
    @Column("booking_notes")
    private String bookingNotes;
    
    @Column("cancelled_at")
    private Instant cancelledAt;
    
    @Column("cancelled_by")
    private UUID cancelledBy;
    
    @Column("cancellation_reason")
    private String cancellationReason;
    
    @Column("refund_amount")
    private BigDecimal refundAmount;
    
    @Column("refund_status")
    private String refundStatus;
    
    @Column("rescheduled_from_id")
    private UUID rescheduledFromId;
    
    @Column("rescheduled_to_id")
    private UUID rescheduledToId;
    
    @Column("reschedule_count")
    private Integer rescheduleCount;
    
    @Column("consultation_started_at")
    private Instant consultationStartedAt;
    
    @Column("consultation_ended_at")
    private Instant consultationEndedAt;
    
    @Column("consultation_id")
    private UUID consultationId;
    
    @Column("is_followup")
    private Boolean isFollowup;
    
    @Column("original_appointment_id")
    private UUID originalAppointmentId;
    
    @Column("followup_scheduled")
    private Boolean followupScheduled;
    
    @Column("reserved_until")
    private Instant reservedUntil;
    
    @Column("created_at")
    private Instant createdAt;
    
    @Column("updated_at")
    private Instant updatedAt;
}
