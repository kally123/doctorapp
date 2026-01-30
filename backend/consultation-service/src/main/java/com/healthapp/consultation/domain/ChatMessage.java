package com.healthapp.consultation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Chat message stored in MongoDB for real-time consultation chat.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
@CompoundIndexes({
    @CompoundIndex(name = "session_messages_desc", def = "{'sessionId': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "session_sender", def = "{'sessionId': 1, 'senderId': 1}")
})
public class ChatMessage {
    
    @Id
    private String id;
    
    @Indexed
    private String sessionId;
    
    private String senderId;
    
    private String senderType; // PATIENT, DOCTOR
    
    private String senderName;
    
    private String messageType; // TEXT, IMAGE, FILE, SYSTEM
    
    // For text messages
    private String content;
    
    // For file/image messages
    private Attachment attachment;
    
    // Message status
    private String status; // SENT, DELIVERED, READ
    
    private Instant deliveredAt;
    
    private Instant readAt;
    
    private Boolean isDeleted;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
    
    /**
     * Attachment details for file/image messages.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        private String fileName;
        private String fileUrl;
        private Long fileSize;
        private String mimeType;
        private String thumbnailUrl;
    }
    
    // Helper methods
    public MessageType getMessageTypeEnum() {
        return messageType != null ? MessageType.valueOf(messageType) : null;
    }
    
    public void setMessageTypeEnum(MessageType type) {
        this.messageType = type != null ? type.name() : null;
    }
    
    public MessageStatus getStatusEnum() {
        return status != null ? MessageStatus.valueOf(status) : null;
    }
    
    public void setStatusEnum(MessageStatus messageStatus) {
        this.status = messageStatus != null ? messageStatus.name() : null;
    }
    
    public ParticipantType getSenderTypeEnum() {
        return senderType != null ? ParticipantType.valueOf(senderType) : null;
    }
    
    public void setSenderTypeEnum(ParticipantType type) {
        this.senderType = type != null ? type.name() : null;
    }
}
