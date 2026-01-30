package com.healthapp.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthapp.notification.domain.NotificationChannel;
import com.healthapp.notification.domain.NotificationStatus;
import com.healthapp.notification.domain.NotificationType;
import com.healthapp.notification.domain.ScheduledNotification;
import com.healthapp.notification.dto.NotificationRequest;
import com.healthapp.notification.repository.ScheduledNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReminderScheduler {
    
    private final ScheduledNotificationRepository scheduledRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    private static final int BATCH_SIZE = 100;
    
    @Scheduled(fixedRateString = "${notification.scheduler.rate:60000}") // Default: every minute
    public void processScheduledNotifications() {
        log.debug("Processing scheduled notifications...");
        
        Instant now = Instant.now();
        
        scheduledRepository.findDueNotifications(now, BATCH_SIZE)
                .flatMap(this::processScheduledNotification)
                .doOnComplete(() -> log.debug("Scheduled notification processing complete"))
                .doOnError(e -> log.error("Error processing scheduled notifications", e))
                .subscribe();
    }
    
    private Mono<ScheduledNotification> processScheduledNotification(ScheduledNotification scheduled) {
        return Mono.defer(() -> {
            try {
                NotificationRequest request = buildRequest(scheduled);
                
                return notificationService.send(request)
                        .then(Mono.defer(() -> {
                            scheduled.setStatus(NotificationStatus.SENT.name());
                            scheduled.setSentAt(Instant.now());
                            return scheduledRepository.save(scheduled);
                        }))
                        .onErrorResume(e -> {
                            log.error("Failed to send scheduled notification: {}", scheduled.getId(), e);
                            scheduled.setStatus(NotificationStatus.FAILED.name());
                            scheduled.setErrorMessage(e.getMessage());
                            return scheduledRepository.save(scheduled);
                        });
            } catch (Exception e) {
                log.error("Error building notification request for: {}", scheduled.getId(), e);
                scheduled.setStatus(NotificationStatus.FAILED.name());
                scheduled.setErrorMessage("Failed to build request: " + e.getMessage());
                return scheduledRepository.save(scheduled);
            }
        });
    }
    
    private NotificationRequest buildRequest(ScheduledNotification scheduled) {
        Map<String, Object> context = null;
        if (scheduled.getContext() != null) {
            try {
                context = objectMapper.readValue(scheduled.getContext(), Map.class);
            } catch (Exception e) {
                log.warn("Failed to parse context for scheduled notification: {}", scheduled.getId());
            }
        }
        
        return NotificationRequest.builder()
                .userId(scheduled.getUserId())
                .notificationType(NotificationType.valueOf(scheduled.getNotificationType()))
                .channel(NotificationChannel.valueOf(scheduled.getChannel()))
                .referenceType(scheduled.getReferenceType())
                .referenceId(scheduled.getReferenceId())
                .context(context)
                .build();
    }
    
    @Scheduled(cron = "${notification.scheduler.cleanup-cron:0 0 2 * * ?}") // Default: 2 AM daily
    public void cleanupOldNotifications() {
        log.info("Cleaning up old processed notifications...");
        
        Instant cutoff = Instant.now().minus(Duration.ofDays(7));
        
        scheduledRepository.deleteOldProcessedNotifications(cutoff)
                .doOnSuccess(count -> log.info("Deleted {} old processed notifications", count))
                .doOnError(e -> log.error("Error cleaning up old notifications", e))
                .subscribe();
    }
}
