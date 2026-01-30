package com.healthapp.notification.controller;

import com.healthapp.notification.dto.*;
import com.healthapp.notification.service.DeviceService;
import com.healthapp.notification.service.NotificationService;
import com.healthapp.notification.service.UserPreferencesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    private final UserPreferencesService preferencesService;
    private final DeviceService deviceService;
    
    // Notification endpoints
    
    @PostMapping("/send")
    public Mono<ResponseEntity<NotificationDto>> sendNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("Sending notification to user: {}", request.getUserId());
        return notificationService.send(request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }
    
    @GetMapping("/user/{userId}")
    public Flux<NotificationDto> getUserNotifications(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "20") int limit) {
        return notificationService.getUserNotifications(userId, limit);
    }
    
    @DeleteMapping("/scheduled")
    public Mono<ResponseEntity<Void>> cancelScheduledNotifications(
            @RequestParam String referenceType,
            @RequestParam String referenceId) {
        return notificationService.cancelScheduledNotifications(referenceType, referenceId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
    
    // User preferences endpoints
    
    @GetMapping("/preferences/{userId}")
    public Mono<ResponseEntity<NotificationPreferencesDto>> getPreferences(@PathVariable UUID userId) {
        return preferencesService.getPreferences(userId)
                .map(ResponseEntity::ok);
    }
    
    @PutMapping("/preferences")
    public Mono<ResponseEntity<NotificationPreferencesDto>> updatePreferences(
            @Valid @RequestBody NotificationPreferencesRequest request) {
        return preferencesService.updatePreferences(request)
                .map(ResponseEntity::ok);
    }
    
    // Device registration endpoints
    
    @PostMapping("/devices/register")
    public Mono<ResponseEntity<DeviceDto>> registerDevice(@Valid @RequestBody DeviceRegistrationRequest request) {
        log.info("Registering device for user: {}", request.getUserId());
        return deviceService.registerDevice(request)
                .map(device -> ResponseEntity.status(HttpStatus.CREATED).body(device));
    }
    
    @DeleteMapping("/devices/{deviceToken}")
    public Mono<ResponseEntity<Void>> unregisterDevice(@PathVariable String deviceToken) {
        return deviceService.unregisterDevice(deviceToken)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
    
    @GetMapping("/devices/user/{userId}")
    public Flux<DeviceDto> getUserDevices(@PathVariable UUID userId) {
        return deviceService.getUserDevices(userId);
    }
    
    @DeleteMapping("/devices/user/{userId}/logout-all")
    public Mono<ResponseEntity<Void>> logoutAllDevices(@PathVariable UUID userId) {
        return deviceService.logoutAllDevices(userId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
    
    // Health check
    
    @GetMapping("/health")
    public Mono<ResponseEntity<String>> health() {
        return Mono.just(ResponseEntity.ok("OK"));
    }
}
