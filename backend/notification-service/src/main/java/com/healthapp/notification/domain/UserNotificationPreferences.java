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
@Table("user_notification_preferences")
public class UserNotificationPreferences {
    
    @Id
    private UUID id;
    
    @Column("user_id")
    private UUID userId;
    
    @Column("email_enabled")
    private Boolean emailEnabled;
    
    @Column("sms_enabled")
    private Boolean smsEnabled;
    
    @Column("push_enabled")
    private Boolean pushEnabled;
    
    @Column("marketing_email")
    private Boolean marketingEmail;
    
    @Column("marketing_sms")
    private Boolean marketingSms;
    
    @Column("reminder_24h")
    private Boolean reminder24h;
    
    @Column("reminder_1h")
    private Boolean reminder1h;
    
    @Column("reminder_15min")
    private Boolean reminder15min;
    
    @Column("created_at")
    private Instant createdAt;
    
    @Column("updated_at")
    private Instant updatedAt;
}
