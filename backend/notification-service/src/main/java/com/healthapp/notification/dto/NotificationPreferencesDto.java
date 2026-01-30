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
public class NotificationPreferencesDto {
    private UUID id;
    private UUID userId;
    private Boolean emailEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private Boolean marketingEmail;
    private Boolean marketingSms;
    private Boolean reminder24h;
    private Boolean reminder1h;
    private Boolean reminder15min;
    private Instant updatedAt;
}
