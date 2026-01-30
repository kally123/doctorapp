package com.healthapp.consultation.controller;

import com.healthapp.consultation.dto.ChatMessageResponse;
import com.healthapp.consultation.dto.SendMessageRequest;
import com.healthapp.consultation.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * REST controller for chat message management.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat message APIs")
public class ChatController {
    
    private final ChatService chatService;
    
    @PostMapping("/messages")
    @Operation(summary = "Send a message", description = "Sends a chat message to a session")
    public Mono<ResponseEntity<ChatMessageResponse>> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return chatService.sendMessage(request)
                .map(ResponseEntity::ok);
    }
    
    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "Get chat history", description = "Gets paginated chat history for a session")
    public Flux<ChatMessageResponse> getChatHistory(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return chatService.getChatHistory(sessionId, page, size);
    }
    
    @GetMapping("/sessions/{sessionId}/messages/since")
    @Operation(summary = "Get messages since timestamp", description = "Gets messages since a specific timestamp")
    public Flux<ChatMessageResponse> getMessagesSince(
            @PathVariable String sessionId,
            @RequestParam Instant since) {
        return chatService.getMessagesSince(sessionId, since);
    }
    
    @GetMapping("/sessions/{sessionId}/count")
    @Operation(summary = "Get message count", description = "Gets total message count for a session")
    public Mono<ResponseEntity<Long>> getMessageCount(@PathVariable String sessionId) {
        return chatService.getMessageCount(sessionId)
                .map(ResponseEntity::ok);
    }
    
    @PostMapping("/sessions/{sessionId}/read")
    @Operation(summary = "Mark messages as read", description = "Marks all unread messages as read for a user")
    public Mono<ResponseEntity<Void>> markAsRead(
            @PathVariable String sessionId,
            @RequestParam String userId) {
        return chatService.markAsRead(sessionId, userId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
}
