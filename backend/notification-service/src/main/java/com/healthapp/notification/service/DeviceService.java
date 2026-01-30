package com.healthapp.notification.service;

import com.healthapp.notification.domain.UserDevice;
import com.healthapp.notification.dto.DeviceDto;
import com.healthapp.notification.dto.DeviceRegistrationRequest;
import com.healthapp.notification.repository.UserDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceService {
    
    private final UserDeviceRepository deviceRepository;
    
    public Mono<DeviceDto> registerDevice(DeviceRegistrationRequest request) {
        return deviceRepository.findByDeviceToken(request.getDeviceToken())
                .flatMap(existing -> {
                    // Update existing device registration
                    existing.setUserId(request.getUserId());
                    existing.setDeviceType(request.getDeviceType());
                    existing.setDeviceName(request.getDeviceName());
                    existing.setAppVersion(request.getAppVersion());
                    existing.setOsVersion(request.getOsVersion());
                    existing.setIsActive(true);
                    existing.setLastUsedAt(Instant.now());
                    existing.setUpdatedAt(Instant.now());
                    return deviceRepository.save(existing);
                })
                .switchIfEmpty(
                    Mono.defer(() -> {
                        UserDevice newDevice = UserDevice.builder()
                                .id(UUID.randomUUID())
                                .userId(request.getUserId())
                                .deviceToken(request.getDeviceToken())
                                .deviceType(request.getDeviceType())
                                .deviceName(request.getDeviceName())
                                .appVersion(request.getAppVersion())
                                .osVersion(request.getOsVersion())
                                .isActive(true)
                                .lastUsedAt(Instant.now())
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
                        return deviceRepository.save(newDevice);
                    })
                )
                .map(this::toDto)
                .doOnSuccess(device -> log.info("Device registered for user: {}", request.getUserId()));
    }
    
    public Mono<Void> unregisterDevice(String deviceToken) {
        return deviceRepository.deactivateByDeviceToken(deviceToken)
                .doOnSuccess(count -> log.info("Deactivated device: {}", deviceToken))
                .then();
    }
    
    public Flux<DeviceDto> getUserDevices(UUID userId) {
        return deviceRepository.findByUserIdAndIsActiveTrue(userId)
                .map(this::toDto);
    }
    
    public Mono<Void> updateLastUsed(UUID deviceId) {
        return deviceRepository.updateLastUsedAt(deviceId, Instant.now())
                .then();
    }
    
    public Mono<Void> logoutAllDevices(UUID userId) {
        return deviceRepository.deactivateAllForUser(userId)
                .doOnSuccess(count -> log.info("Deactivated {} devices for user: {}", count, userId))
                .then();
    }
    
    private DeviceDto toDto(UserDevice device) {
        return DeviceDto.builder()
                .id(device.getId())
                .userId(device.getUserId())
                .deviceToken(device.getDeviceToken())
                .deviceType(device.getDeviceType())
                .deviceName(device.getDeviceName())
                .appVersion(device.getAppVersion())
                .osVersion(device.getOsVersion())
                .isActive(device.getIsActive())
                .lastUsedAt(device.getLastUsedAt())
                .createdAt(device.getCreatedAt())
                .build();
    }
}
