package com.healthapp.notification.repository;

import com.healthapp.notification.domain.NotificationLog;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends ReactiveCrudRepository<NotificationLog, UUID> {
    
    Flux<NotificationLog> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    Flux<NotificationLog> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);
    
    Flux<NotificationLog> findByStatusAndCreatedAtAfter(String status, Instant after);
    
    Flux<NotificationLog> findByUserIdAndChannelOrderByCreatedAtDesc(UUID userId, String channel);
    
    Flux<NotificationLog> findByUserIdAndNotificationTypeOrderByCreatedAtDesc(UUID userId, String notificationType);
    
    @Query("SELECT * FROM notification_logs WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    Flux<NotificationLog> findRecentByUserId(UUID userId, int limit);
    
    @Query("SELECT COUNT(*) FROM notification_logs WHERE user_id = :userId AND status = 'SENT' AND created_at > :since")
    Mono<Long> countSentNotificationsSince(UUID userId, Instant since);
    
    @Query("SELECT COUNT(*) FROM notification_logs WHERE status = 'FAILED' AND created_at > :since")
    Mono<Long> countFailedNotificationsSince(Instant since);
}
