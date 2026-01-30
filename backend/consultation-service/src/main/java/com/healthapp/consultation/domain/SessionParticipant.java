package com.healthapp.consultation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks participant details and connection metrics for a session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("session_participants")
public class SessionParticipant {
    
    @Id
    private UUID id;
    
    @Column("session_id")
    private UUID sessionId;
    
    @Column("user_id")
    private UUID userId;
    
    @Column("participant_type")
    private String participantType;
    
    // Connection events
    @Column("joined_at")
    private Instant joinedAt;
    
    @Column("left_at")
    private Instant leftAt;
    
    @Column("rejoin_count")
    private Integer rejoinCount;
    
    // Device info
    @Column("device_type")
    private String deviceType;
    
    @Column("browser")
    private String browser;
    
    @Column("os")
    private String os;
    
    // Connection quality log
    @Column("avg_audio_level")
    private Float avgAudioLevel;
    
    @Column("avg_video_bitrate")
    private Integer avgVideoBitrate;
    
    @Column("packet_loss_percent")
    private Float packetLossPercent;
    
    @CreatedDate
    @Column("created_at")
    private Instant createdAt;
    
    // Helper methods
    public ParticipantType getParticipantTypeEnum() {
        return participantType != null ? ParticipantType.valueOf(participantType) : null;
    }
    
    public void setParticipantTypeEnum(ParticipantType type) {
        this.participantType = type != null ? type.name() : null;
    }
}
