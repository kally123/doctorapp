package com.healthapp.notification.repository;

import com.healthapp.notification.domain.UserNotificationPreferences;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserNotificationPreferencesRepository extends ReactiveCrudRepository<UserNotificationPreferences, UUID> {
    
    Mono<UserNotificationPreferences> findByUserId(UUID userId);
    
    Mono<Boolean> existsByUserId(UUID userId);
}
