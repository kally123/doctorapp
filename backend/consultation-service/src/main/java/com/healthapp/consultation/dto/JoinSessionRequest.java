package com.healthapp.consultation.dto;

import com.healthapp.consultation.domain.ParticipantType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request to join a consultation session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinSessionRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Participant type is required")
    private ParticipantType participantType;
    
    @NotBlank(message = "Display name is required")
    private String displayName;
    
    // Optional device info
    private String deviceType;
    private String browser;
    private String os;
}
