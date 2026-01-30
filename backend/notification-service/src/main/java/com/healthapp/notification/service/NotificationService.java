package com.healthapp.notification.service;

import com.healthapp.notification.domain.*;
import com.healthapp.notification.dto.NotificationDto;
import com.healthapp.notification.dto.NotificationRequest;
import com.healthapp.notification.provider.EmailProvider;
import com.healthapp.notification.provider.PushProvider;
import com.healthapp.notification.provider.SmsProvider;
import com.healthapp.notification.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationLogRepository logRepository;
    private final ScheduledNotificationRepository scheduledRepository;
    private final UserNotificationPreferencesRepository preferencesRepository;
    private final UserDeviceRepository deviceRepository;
    private final TemplateService templateService;
    private final EmailProvider emailProvider;
    private final SmsProvider smsProvider;
    private final PushProvider pushProvider;
    
    public Mono<NotificationDto> send(NotificationRequest request) {
        // If scheduled for future, save and return
        if (request.getScheduledFor() != null && request.getScheduledFor().isAfter(Instant.now())) {
            return scheduleNotification(request);
        }
        
        // Check user preferences first
        return checkUserPreferences(request.getUserId(), request.getChannel())
                .flatMap(allowed -> {
                    if (!allowed) {
                        log.info("User {} has disabled {} notifications", request.getUserId(), request.getChannel());
                        return Mono.empty();
                    }
                    return sendImmediate(request);
                });
    }
    
    public Mono<List<NotificationDto>> sendMultiChannel(UUID userId, NotificationType type,
                                                         Map<String, Object> context, String referenceType, String referenceId) {
        List<NotificationChannel> channels = List.of(NotificationChannel.EMAIL, NotificationChannel.PUSH);
        
        return Flux.fromIterable(channels)
                .flatMap(channel -> {
                    NotificationRequest request = NotificationRequest.builder()
                            .userId(userId)
                            .notificationType(type)
                            .channel(channel)
                            .referenceType(referenceType)
                            .referenceId(referenceId)
                            .context(context)
                            .build();
                    return send(request);
                })
                .collectList();
    }
    
    private Mono<NotificationDto> sendImmediate(NotificationRequest request) {
        return templateService.processTemplate(
                        request.getNotificationType(),
                        request.getChannel(),
                        request.getLocale(),
                        request.getContext())
                .flatMap(processedTemplate -> {
                    NotificationLog notificationLog = NotificationLog.builder()
                            .id(UUID.randomUUID())
                            .userId(request.getUserId())
                            .notificationType(request.getNotificationType().name())
                            .channel(request.getChannel().name())
                            .recipient(request.getRecipient())
                            .subject(processedTemplate.getSubject())
                            .body(processedTemplate.getBody())
                            .status(NotificationStatus.PENDING.name())
                            .referenceType(request.getReferenceType())
                            .referenceId(request.getReferenceId())
                            .templateId(processedTemplate.getTemplateId())
                            .createdAt(Instant.now())
                            .build();
                    
                    return logRepository.save(notificationLog)
                            .flatMap(saved -> sendViaChannel(saved, request.getChannel(), processedTemplate)
                                    .flatMap(success -> {
                                        saved.setStatus(success ? NotificationStatus.SENT.name() : NotificationStatus.FAILED.name());
                                        saved.setSentAt(success ? Instant.now() : null);
                                        if (!success) {
                                            saved.setErrorMessage("Failed to send via " + request.getChannel());
                                        }
                                        return logRepository.save(saved);
                                    }))
                            .map(this::toDto);
                })
                .doOnError(e -> log.error("Error sending notification", e));
    }
    
    private Mono<Boolean> sendViaChannel(NotificationLog notificationLog, NotificationChannel channel, 
                                          TemplateService.ProcessedTemplate template) {
        Map<String, Object> metadata = new HashMap<>();
        
        switch (channel) {
            case EMAIL:
                return emailProvider.sendEmail(notificationLog.getRecipient(), template.getSubject(), template.getBody(), metadata);
            
            case SMS:
                return smsProvider.sendSms(notificationLog.getRecipient(), template.getBody(), metadata);
            
            case PUSH:
                return deviceRepository.findByUserIdAndIsActiveTrue(notificationLog.getUserId())
                        .collectList()
                        .flatMap(devices -> {
                            if (devices.isEmpty()) {
                                log.warn("No active devices found for user: {}", notificationLog.getUserId());
                                return Mono.just(false);
                            }
                            
                            Map<String, String> data = new HashMap<>();
                            data.put("type", notificationLog.getNotificationType());
                            if (notificationLog.getReferenceType() != null) {
                                data.put("referenceType", notificationLog.getReferenceType());
                            }
                            if (notificationLog.getReferenceId() != null) {
                                data.put("referenceId", notificationLog.getReferenceId());
                            }
                            
                            List<String> tokens = devices.stream()
                                    .map(UserDevice::getDeviceToken)
                                    .collect(Collectors.toList());
                            
                            return pushProvider.sendPushToMultiple(tokens, template.getSubject(), template.getBody(), data)
                                    .map(sent -> sent > 0);
                        });
            
            case IN_APP:
                // In-app notifications are stored in the log and displayed in the app
                return Mono.just(true);
            
            default:
                return Mono.just(false);
        }
    }
    
    private Mono<NotificationDto> scheduleNotification(NotificationRequest request) {
        ScheduledNotification scheduled = ScheduledNotification.builder()
                .id(UUID.randomUUID())
                .userId(request.getUserId())
                .notificationType(request.getNotificationType().name())
                .channel(request.getChannel().name())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .scheduledFor(request.getScheduledFor())
                .status(NotificationStatus.PENDING.name())
                .context(serializeContext(request.getContext()))
                .createdAt(Instant.now())
                .build();
        
        return scheduledRepository.save(scheduled)
                .map(saved -> NotificationDto.builder()
                        .id(saved.getId())
                        .userId(saved.getUserId())
                        .notificationType(saved.getNotificationType())
                        .channel(saved.getChannel())
                        .status(NotificationStatus.PENDING.name())
                        .referenceType(saved.getReferenceType())
                        .referenceId(saved.getReferenceId())
                        .createdAt(saved.getCreatedAt())
                        .build());
    }
    
    private Mono<Boolean> checkUserPreferences(UUID userId, NotificationChannel channel) {
        return preferencesRepository.findByUserId(userId)
                .map(prefs -> {
                    switch (channel) {
                        case EMAIL:
                            return prefs.getEmailEnabled() != null ? prefs.getEmailEnabled() : true;
                        case SMS:
                            return prefs.getSmsEnabled() != null ? prefs.getSmsEnabled() : true;
                        case PUSH:
                            return prefs.getPushEnabled() != null ? prefs.getPushEnabled() : true;
                        default:
                            return true;
                    }
                })
                .defaultIfEmpty(true); // Default to enabled if no preferences set
    }
    
    public Flux<NotificationDto> getUserNotifications(UUID userId, int limit) {
        return logRepository.findRecentByUserId(userId, limit)
                .map(this::toDto);
    }
    
    public Mono<Void> cancelScheduledNotifications(String referenceType, String referenceId) {
        return scheduledRepository.cancelByReference(referenceType, referenceId, NotificationStatus.FAILED.name())
                .doOnSuccess(count -> log.info("Cancelled {} scheduled notifications for {}/{}", 
                        count, referenceType, referenceId))
                .then();
    }
    
    private String serializeContext(Map<String, Object> context) {
        if (context == null) return null;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(context);
        } catch (Exception e) {
            log.error("Failed to serialize context", e);
            return null;
        }
    }
    
    private NotificationDto toDto(NotificationLog notificationLog) {
        return NotificationDto.builder()
                .id(notificationLog.getId())
                .userId(notificationLog.getUserId())
                .notificationType(notificationLog.getNotificationType())
                .channel(notificationLog.getChannel())
                .recipient(notificationLog.getRecipient())
                .subject(notificationLog.getSubject())
                .status(notificationLog.getStatus())
                .referenceType(notificationLog.getReferenceType())
                .referenceId(notificationLog.getReferenceId())
                .sentAt(notificationLog.getSentAt())
                .createdAt(notificationLog.getCreatedAt())
                .build();
    }
}
