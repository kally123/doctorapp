package com.healthapp.consultation.service;

import org.springframework.context.annotation.Profile;
import com.healthapp.consultation.domain.ChatMessage;
import com.healthapp.consultation.domain.MessageStatus;
import com.healthapp.consultation.domain.MessageType;
import com.healthapp.consultation.dto.ChatMessageResponse;
import com.healthapp.consultation.dto.SendMessageRequest;
import com.healthapp.consultation.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Service for handling real-time chat during consultations.
 */
@Slf4j
@Profile("!test")
@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final ChatMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Sends a chat message and broadcasts to session participants.
     */
    public Mono<ChatMessageResponse> sendMessage(SendMessageRequest request) {
        ChatMessage message = ChatMessage.builder()
                .sessionId(request.getSessionId())
                .senderId(request.getSenderId())
                .senderType(request.getSenderType().name())
                .senderName(request.getSenderName())
                .messageType(request.getMessageType().name())
                .content(request.getContent())
                .status(MessageStatus.SENT.name())
                .isDeleted(false)
                .build();
        
        // Handle attachment if present
        if (request.getAttachment() != null) {
            message.setAttachment(ChatMessage.Attachment.builder()
                    .fileName(request.getAttachment().getFileName())
                    .fileUrl(request.getAttachment().getFileUrl())
                    .fileSize(request.getAttachment().getFileSize())
                    .mimeType(request.getAttachment().getMimeType())
                    .thumbnailUrl(request.getAttachment().getThumbnailUrl())
                    .build());
        }
        
        return messageRepository.save(message)
                .map(this::mapToResponse)
                .doOnSuccess(response -> {
                    // Broadcast message to session topic
                    String destination = "/topic/session." + request.getSessionId();
                    messagingTemplate.convertAndSend(destination, response);
                    log.debug("Sent message to session: {}", request.getSessionId());
                });
    }
    
    /**
     * Gets chat history for a session.
     */
    public Flux<ChatMessageResponse> getChatHistory(String sessionId, int page, int size) {
        return messageRepository.findBySessionIdOrderByCreatedAtDesc(sessionId, PageRequest.of(page, size))
                .map(this::mapToResponse);
    }
    
    /**
     * Gets recent messages since a timestamp.
     */
    public Flux<ChatMessageResponse> getMessagesSince(String sessionId, Instant since) {
        return messageRepository.findBySessionIdAndCreatedAtAfterOrderByCreatedAtAsc(sessionId, since)
                .map(this::mapToResponse);
    }
    
    /**
     * Marks messages as delivered for a recipient.
     */
    public Mono<Void> markAsDelivered(String sessionId, String recipientId) {
        return messageRepository.findBySessionIdAndStatusNot(sessionId, MessageStatus.READ.name())
                .filter(msg -> !msg.getSenderId().equals(recipientId))
                .flatMap(msg -> {
                    if (MessageStatus.SENT.name().equals(msg.getStatus())) {
                        msg.setStatus(MessageStatus.DELIVERED.name());
                        msg.setDeliveredAt(Instant.now());
                        return messageRepository.save(msg);
                    }
                    return Mono.just(msg);
                })
                .then();
    }
    
    /**
     * Marks messages as read by a recipient.
     */
    public Mono<Void> markAsRead(String sessionId, String recipientId) {
        return messageRepository.findBySessionIdAndStatusNot(sessionId, MessageStatus.READ.name())
                .filter(msg -> !msg.getSenderId().equals(recipientId))
                .flatMap(msg -> {
                    msg.setStatus(MessageStatus.READ.name());
                    msg.setReadAt(Instant.now());
                    return messageRepository.save(msg);
                })
                .then()
                .doOnSuccess(v -> {
                    // Notify sender that messages were read
                    messagingTemplate.convertAndSend("/topic/session." + sessionId + ".read", 
                            new ReadReceiptEvent(sessionId, recipientId, Instant.now()));
                });
    }
    
    /**
     * Gets message count for a session.
     */
    public Mono<Long> getMessageCount(String sessionId) {
        return messageRepository.countBySessionId(sessionId);
    }
    
    /**
     * Sends a system message (e.g., "Doctor has joined").
     */
    public Mono<ChatMessageResponse> sendSystemMessage(String sessionId, String content) {
        ChatMessage message = ChatMessage.builder()
                .sessionId(sessionId)
                .senderId("SYSTEM")
                .senderType("SYSTEM")
                .senderName("System")
                .messageType(MessageType.SYSTEM.name())
                .content(content)
                .status(MessageStatus.SENT.name())
                .isDeleted(false)
                .build();
        
        return messageRepository.save(message)
                .map(this::mapToResponse)
                .doOnSuccess(response -> {
                    String destination = "/topic/session." + sessionId;
                    messagingTemplate.convertAndSend(destination, response);
                });
    }
    
    private ChatMessageResponse mapToResponse(ChatMessage message) {
        ChatMessageResponse.AttachmentDto attachmentDto = null;
        if (message.getAttachment() != null) {
            attachmentDto = ChatMessageResponse.AttachmentDto.builder()
                    .fileName(message.getAttachment().getFileName())
                    .fileUrl(message.getAttachment().getFileUrl())
                    .fileSize(message.getAttachment().getFileSize())
                    .mimeType(message.getAttachment().getMimeType())
                    .thumbnailUrl(message.getAttachment().getThumbnailUrl())
                    .build();
        }
        
        return ChatMessageResponse.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .senderId(message.getSenderId())
                .senderType(message.getSenderTypeEnum())
                .senderName(message.getSenderName())
                .messageType(message.getMessageTypeEnum())
                .content(message.getContent())
                .attachment(attachmentDto)
                .status(message.getStatusEnum())
                .deliveredAt(message.getDeliveredAt())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
    
    /**
     * Event for read receipts.
     */
    public record ReadReceiptEvent(String sessionId, String readerId, Instant readAt) {}
}
