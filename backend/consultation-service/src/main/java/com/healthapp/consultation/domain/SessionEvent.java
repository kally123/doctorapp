package com.healthapp.consultation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Session event log for analytics and debugging.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("session_events")
public class SessionEvent {
    
    @Id
    private UUID id;
    
    @Column("session_id")
    private UUID sessionId;
    
    @Column("event_type")
    private String eventType;
    
    @Column("user_id")
    private UUID userId;
    
    @Column("event_data")
    private String eventData; // JSON string
    
    @CreatedDate
    @Column("created_at")
    private Instant createdAt;
}
