package com.healthapp.appointment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entity representing an available time slot for booking
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("available_slots")
public class AvailableSlot {
    
    @Id
    private UUID id;
    
    @Column("doctor_id")
    private UUID doctorId;
    
    @Column("clinic_id")
    private UUID clinicId;
    
    @Column("slot_date")
    private LocalDate slotDate;
    
    @Column("start_time")
    private LocalTime startTime;
    
    @Column("end_time")
    private LocalTime endTime;
    
    @Column("consultation_type")
    private ConsultationType consultationType;
    
    @Column("slot_duration_minutes")
    private Integer slotDurationMinutes;
    
    @Column("status")
    private SlotStatus status;
    
    @Column("appointment_id")
    private UUID appointmentId;
    
    @Column("created_at")
    private Instant createdAt;
}
