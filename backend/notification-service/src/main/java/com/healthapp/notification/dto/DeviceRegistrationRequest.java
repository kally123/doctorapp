package com.healthapp.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRegistrationRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotBlank(message = "Device token is required")
    private String deviceToken;
    
    @NotBlank(message = "Device type is required")
    private String deviceType; // ios, android, web
    
    private String deviceName;
    
    private String appVersion;
    
    private String osVersion;
}
