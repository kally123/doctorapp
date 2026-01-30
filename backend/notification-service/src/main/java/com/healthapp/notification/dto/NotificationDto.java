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
public class NotificationDto {
    private UUID id;
    private UUID userId;
    private String notificationType;
    private String channel;
    private String recipient;
    private String subject;
    private String status;
    private String referenceType;
    private String referenceId;
    private Instant sentAt;
    private Instant createdAt;
}
