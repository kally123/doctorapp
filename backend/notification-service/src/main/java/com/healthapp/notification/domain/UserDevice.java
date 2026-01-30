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
@Table("user_devices")
public class UserDevice {
    
    @Id
    private UUID id;
    
    @Column("user_id")
    private UUID userId;
    
    @Column("device_token")
    private String deviceToken;
    
    @Column("device_type")
    private String deviceType;
    
    @Column("device_name")
    private String deviceName;
    
    @Column("app_version")
    private String appVersion;
    
    @Column("os_version")
    private String osVersion;
    
    @Column("is_active")
    private Boolean isActive;
    
    @Column("last_used_at")
    private Instant lastUsedAt;
    
    @Column("created_at")
    private Instant createdAt;
    
    @Column("updated_at")
    private Instant updatedAt;
}
