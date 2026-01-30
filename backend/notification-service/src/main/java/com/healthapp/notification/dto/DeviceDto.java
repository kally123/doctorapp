package com.healthapp.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDto {
    private UUID id;
    private UUID userId;
    private String deviceToken;
    private String deviceType;
    private String deviceName;
    private String appVersion;
    private String osVersion;
    private Boolean isActive;
    private Instant lastUsedAt;
    private Instant createdAt;
}
