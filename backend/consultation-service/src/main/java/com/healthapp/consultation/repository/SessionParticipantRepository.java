package com.healthapp.consultation.repository;

import com.healthapp.consultation.domain.SessionParticipant;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface SessionParticipantRepository extends ReactiveCrudRepository<SessionParticipant, UUID> {
    
    Flux<SessionParticipant> findBySessionId(UUID sessionId);
    
    Mono<SessionParticipant> findBySessionIdAndUserId(UUID sessionId, UUID userId);
    
    Flux<SessionParticipant> findBySessionIdAndParticipantType(UUID sessionId, String participantType);
    
    Mono<Boolean> existsBySessionIdAndUserId(UUID sessionId, UUID userId);
}
