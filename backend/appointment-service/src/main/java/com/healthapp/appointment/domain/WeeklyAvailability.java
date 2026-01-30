package com.healthapp.appointment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entity representing weekly recurring availability for a doctor
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("weekly_availability")
public class WeeklyAvailability {
    
    @Id
    private UUID id;
    
    @Column("doctor_id")
    private UUID doctorId;
    
    @Column("clinic_id")
    private UUID clinicId;
    
    @Column("day_of_week")
    private Integer dayOfWeek;
    
    @Column("start_time")
    private LocalTime startTime;
    
    @Column("end_time")
    private LocalTime endTime;
    
    @Column("slot_duration_minutes")
    private Integer slotDurationMinutes;
    
    @Column("buffer_minutes")
    private Integer bufferMinutes;
    
    @Column("consultation_type")
    private ConsultationType consultationType;
    
    @Column("max_patients_per_slot")
    private Integer maxPatientsPerSlot;
    
    @Column("is_active")
    private Boolean isActive;
    
    @Column("created_at")
    private Instant createdAt;
    
    @Column("updated_at")
    private Instant updatedAt;
    
    public DayOfWeek getDayOfWeekEnum() {
        // Java DayOfWeek is 1-7 (Monday-Sunday), database is 0-6 (Sunday-Saturday)
        if (dayOfWeek == 0) return DayOfWeek.SUNDAY;
        return DayOfWeek.of(dayOfWeek);
    }
}
