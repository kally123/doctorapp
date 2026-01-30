package com.healthapp.appointment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity for tracking appointment status changes
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("appointment_status_history")
public class AppointmentStatusHistory {
    
    @Id
    private UUID id;
    
    @Column("appointment_id")
    private UUID appointmentId;
    
    @Column("from_status")
    private AppointmentStatus fromStatus;
    
    @Column("to_status")
    private AppointmentStatus toStatus;
    
    @Column("changed_by")
    private UUID changedBy;
    
    @Column("reason")
    private String reason;
    
    @Column("metadata")
    private String metadata;
    
    @Column("created_at")
    private Instant createdAt;
}
