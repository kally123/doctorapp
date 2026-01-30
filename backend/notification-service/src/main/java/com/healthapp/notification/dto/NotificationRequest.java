package com.healthapp.notification.dto;

import com.healthapp.notification.domain.NotificationChannel;
import com.healthapp.notification.domain.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;
    
    @NotNull(message = "Channel is required")
    private NotificationChannel channel;
    
    private String recipient; // email or phone number
    
    private String referenceType;
    
    private String referenceId;
    
    private Map<String, Object> context;
    
    private Instant scheduledFor;
    
    private String locale;
}
