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
 * Entity for blocked time slots (leave, breaks, etc.)
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("blocked_slots")
public class BlockedSlot {
    
    @Id
    private UUID id;
    
    @Column("doctor_id")
    private UUID doctorId;
    
    @Column("start_datetime")
    private Instant startDatetime;
    
    @Column("end_datetime")
    private Instant endDatetime;
    
    @Column("reason")
    private String reason;
    
    @Column("block_type")
    private String blockType;
    
    @Column("is_recurring")
    private Boolean isRecurring;
    
    @Column("recurrence_pattern")
    private String recurrencePattern;
    
    @Column("created_at")
    private Instant createdAt;
}
