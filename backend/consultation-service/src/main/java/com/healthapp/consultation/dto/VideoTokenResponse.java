package com.healthapp.consultation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response containing Twilio video token for joining a room.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoTokenResponse {
    
    private String token;
    private String roomName;
    private String identity;
    private Instant expiresAt;
    private Integer ttlSeconds;
}
