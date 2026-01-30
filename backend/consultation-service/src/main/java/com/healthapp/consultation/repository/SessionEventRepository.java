package com.healthapp.consultation.repository;

import com.healthapp.consultation.domain.SessionEvent;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface SessionEventRepository extends ReactiveCrudRepository<SessionEvent, UUID> {
    
    Flux<SessionEvent> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
    
    Flux<SessionEvent> findBySessionIdAndEventType(UUID sessionId, String eventType);
    
    @Query("SELECT * FROM session_events WHERE session_id = :sessionId AND created_at > :since ORDER BY created_at")
    Flux<SessionEvent> findEventsSince(UUID sessionId, Instant since);
}
