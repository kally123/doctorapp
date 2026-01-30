package com.healthapp.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("scheduled_notifications")
public class ScheduledNotification {
    
    @Id
    private UUID id;
    
    @Column("user_id")
    private UUID userId;
    
    @Column("notification_type")
    private String notificationType;
    
    @Column("channel")
    private String channel;
    
    @Column("reference_type")
    private String referenceType;
    
    @Column("reference_id")
    private String referenceId;
    
    @Column("scheduled_for")
    private Instant scheduledFor;
    
    @Column("status")
    private String status;
    
    @Column("sent_at")
    private Instant sentAt;
    
    @Column("error_message")
    private String errorMessage;
    
    @Column("context")
    private String context;
    
    @Column("created_at")
    private Instant createdAt;
}
