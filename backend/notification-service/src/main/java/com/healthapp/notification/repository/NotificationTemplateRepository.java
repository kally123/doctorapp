package com.healthapp.notification.repository;

import com.healthapp.notification.domain.NotificationChannel;
import com.healthapp.notification.domain.NotificationTemplate;
import com.healthapp.notification.domain.NotificationType;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends ReactiveCrudRepository<NotificationTemplate, UUID> {
    
    Mono<NotificationTemplate> findByNotificationTypeAndChannelAndLocaleAndIsActiveTrue(
            NotificationType notificationType, 
            NotificationChannel channel, 
            String locale);
    
    Mono<NotificationTemplate> findByNotificationTypeAndChannelAndIsActiveTrue(
            NotificationType notificationType, 
            NotificationChannel channel);
    
    Flux<NotificationTemplate> findByNotificationTypeAndIsActiveTrue(NotificationType notificationType);
    
    Flux<NotificationTemplate> findByChannelAndIsActiveTrue(NotificationChannel channel);
    
    Flux<NotificationTemplate> findByIsActiveTrue();
    
    Mono<Boolean> existsByNotificationTypeAndChannelAndLocale(
            NotificationType notificationType, 
            NotificationChannel channel, 
            String locale);
}
