package com.healthapp.notification.repository;

import com.healthapp.notification.domain.UserDevice;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface UserDeviceRepository extends ReactiveCrudRepository<UserDevice, UUID> {
    
    Flux<UserDevice> findByUserIdAndIsActiveTrue(UUID userId);
    
    Flux<UserDevice> findByUserId(UUID userId);
    
    Mono<UserDevice> findByDeviceToken(String deviceToken);
    
    Mono<Boolean> existsByDeviceToken(String deviceToken);
    
    @Modifying
    @Query("UPDATE user_devices SET is_active = false WHERE device_token = :deviceToken")
    Mono<Integer> deactivateByDeviceToken(String deviceToken);
    
    @Modifying
    @Query("UPDATE user_devices SET is_active = false WHERE user_id = :userId")
    Mono<Integer> deactivateAllForUser(UUID userId);
    
    @Modifying
    @Query("UPDATE user_devices SET last_used_at = :lastUsedAt WHERE id = :id")
    Mono<Integer> updateLastUsedAt(UUID id, Instant lastUsedAt);
    
    @Query("DELETE FROM user_devices WHERE is_active = false AND updated_at < :before")
    Mono<Integer> deleteInactiveDevicesOlderThan(Instant before);
}
