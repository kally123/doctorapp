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
@Table("notification_templates")
public class NotificationTemplate {
    
    @Id
    private UUID id;
    
    @Column("name")
    private String name;
    
    @Column("notification_type")
    private String notificationType;
    
    @Column("channel")
    private String channel;
    
    @Column("subject")
    private String subject;
    
    @Column("body_template")
    private String bodyTemplate;
    
    @Column("locale")
    private String locale;
    
    @Column("is_active")
    private Boolean isActive;
    
    @Column("created_at")
    private Instant createdAt;
    
    @Column("updated_at")
    private Instant updatedAt;
}
