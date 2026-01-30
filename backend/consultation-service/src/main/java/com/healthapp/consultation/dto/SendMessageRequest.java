package com.healthapp.consultation.dto;

import com.healthapp.consultation.domain.MessageType;
import com.healthapp.consultation.domain.ParticipantType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to send a chat message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
    
    @NotBlank(message = "Sender ID is required")
    private String senderId;
    
    @NotNull(message = "Sender type is required")
    private ParticipantType senderType;
    
    @NotBlank(message = "Sender name is required")
    private String senderName;
    
    @NotNull(message = "Message type is required")
    private MessageType messageType;
    
    private String content;
    
    // For file attachments
    private AttachmentDto attachment;
    
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
