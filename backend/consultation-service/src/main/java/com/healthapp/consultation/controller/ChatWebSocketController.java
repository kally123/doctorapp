package com.healthapp.consultation.controller;

import com.healthapp.consultation.dto.ChatMessageResponse;
import com.healthapp.consultation.dto.SendMessageRequest;
import com.healthapp.consultation.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for real-time chat messaging.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    
    private final ChatService chatService;
    
    /**
     * Handles incoming chat messages.
     * Client sends to: /app/chat.send
     * Response sent to: /topic/session.{sessionId}
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request) {
        log.debug("Received message for session: {}", request.getSessionId());
        chatService.sendMessage(request).subscribe();
    }
    
    /**
     * Handles typing indicator.
     * Client sends to: /app/chat.typing.{sessionId}
     * Response sent to: /topic/session.{sessionId}.typing
     */
    @MessageMapping("/chat.typing.{sessionId}")
    @SendTo("/topic/session.{sessionId}.typing")
    public TypingEvent handleTyping(@DestinationVariable String sessionId, @Payload TypingEvent event) {
        log.debug("User {} is typing in session: {}", event.userId(), sessionId);
        return event;
    }
    
    /**
     * Handles message read acknowledgment.
     * Client sends to: /app/chat.read.{sessionId}
     */
    @MessageMapping("/chat.read.{sessionId}")
    public void markAsRead(@DestinationVariable String sessionId, @Payload ReadRequest request) {
        log.debug("Marking messages as read for session: {} by user: {}", sessionId, request.userId());
        chatService.markAsRead(sessionId, request.userId()).subscribe();
    }
    
    /**
     * Subscription handler for session messages.
     */
    @SubscribeMapping("/session.{sessionId}")
    public void subscribeToSession(@DestinationVariable String sessionId) {
        log.debug("Client subscribed to session: {}", sessionId);
    }
    
    /**
     * Typing indicator event.
     */
    public record TypingEvent(String userId, String userName, boolean isTyping) {}
    
    /**
     * Read acknowledgment request.
     */
    public record ReadRequest(String userId) {}
}
