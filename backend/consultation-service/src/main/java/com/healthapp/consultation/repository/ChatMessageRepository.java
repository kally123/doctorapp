package com.healthapp.consultation.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.consultation.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Profile("!test")
@Repository
public interface ChatMessageRepository extends ReactiveMongoRepository<ChatMessage, String> {
    
    Flux<ChatMessage> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    Flux<ChatMessage> findBySessionIdOrderByCreatedAtDesc(String sessionId, Pageable pageable);
    
    Flux<ChatMessage> findBySessionIdAndCreatedAtAfterOrderByCreatedAtAsc(String sessionId, Instant after);
    
    Flux<ChatMessage> findBySessionIdAndSenderIdOrderByCreatedAtDesc(String sessionId, String senderId);
    
    Mono<Long> countBySessionId(String sessionId);
    
    Flux<ChatMessage> findBySessionIdAndStatusNot(String sessionId, String status);
}
