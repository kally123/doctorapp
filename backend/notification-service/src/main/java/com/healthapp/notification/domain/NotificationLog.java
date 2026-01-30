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
@Table("notification_logs")
public class NotificationLog {
    
    @Id
    private UUID id;
    
    @Column("user_id")
    private UUID userId;
    
    @Column("notification_type")
    private String notificationType;
    
    @Column("channel")
    private String channel;
    
    @Column("recipient")
    private String recipient;
    
    @Column("subject")
    private String subject;
    
    @Column("body")
    private String body;
    
    @Column("status")
    private String status;
    
    @Column("reference_type")
    private String referenceType;
    
    @Column("reference_id")
    private String referenceId;
    
    @Column("template_id")
    private UUID templateId;
    
    @Column("provider_response")
    private String providerResponse;
    
    @Column("error_message")
    private String errorMessage;
    
    @Column("created_at")
    private Instant createdAt;
    
    @Column("sent_at")
    private Instant sentAt;
    
    @Column("delivered_at")
    private Instant deliveredAt;
}
