package com.healthapp.consultation.dto;

import com.healthapp.consultation.domain.MessageStatus;
import com.healthapp.consultation.domain.MessageType;
import com.healthapp.consultation.domain.ParticipantType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response containing chat message details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    
    private String id;
    private String sessionId;
    private String senderId;
    private ParticipantType senderType;
    private String senderName;
    private MessageType messageType;
    private String content;
    private AttachmentDto attachment;
    private MessageStatus status;
    private Instant deliveredAt;
    private Instant readAt;
    private Instant createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentDto {
        private String fileName;
        private String fileUrl;
        private Long fileSize;
        private String mimeType;
        private String thumbnailUrl;
    }
}
