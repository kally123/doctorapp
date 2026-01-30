package com.healthapp.notification.repository;

import com.healthapp.notification.domain.ScheduledNotification;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface ScheduledNotificationRepository extends ReactiveCrudRepository<ScheduledNotification, UUID> {
    
    Flux<ScheduledNotification> findByStatusAndScheduledForBefore(String status, Instant scheduledFor);
    
    Flux<ScheduledNotification> findByUserIdAndStatus(UUID userId, String status);
    
    Flux<ScheduledNotification> findByReferenceTypeAndReferenceIdAndStatus(
            String referenceType, 
            String referenceId, 
            String status);
    
    Flux<ScheduledNotification> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);
    
    @Query("SELECT * FROM scheduled_notifications " +
           "WHERE status = 'PENDING' AND scheduled_for <= :now " +
           "ORDER BY scheduled_for ASC LIMIT :limit")
    Flux<ScheduledNotification> findDueNotifications(Instant now, int limit);
    
    @Modifying
    @Query("UPDATE scheduled_notifications SET status = :newStatus WHERE reference_type = :refType AND reference_id = :refId AND status = 'PENDING'")
    Mono<Integer> cancelByReference(String refType, String refId, String newStatus);
    
    @Modifying
    @Query("DELETE FROM scheduled_notifications WHERE status IN ('SENT', 'FAILED') AND scheduled_for < :before")
    Mono<Integer> deleteOldProcessedNotifications(Instant before);
    
    Mono<Boolean> existsByReferenceTypeAndReferenceIdAndNotificationTypeAndChannelAndStatus(
            String referenceType,
            String referenceId,
            String notificationType,
            String channel,
            String status);
}
