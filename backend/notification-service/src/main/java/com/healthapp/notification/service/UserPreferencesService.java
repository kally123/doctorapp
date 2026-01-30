package com.healthapp.notification.service;

import com.healthapp.notification.domain.UserNotificationPreferences;
import com.healthapp.notification.dto.NotificationPreferencesDto;
import com.healthapp.notification.dto.NotificationPreferencesRequest;
import com.healthapp.notification.repository.UserNotificationPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserPreferencesService {
    
    private final UserNotificationPreferencesRepository preferencesRepository;
    
    public Mono<NotificationPreferencesDto> getPreferences(UUID userId) {
        return preferencesRepository.findByUserId(userId)
                .map(this::toDto)
                .switchIfEmpty(createDefaultPreferences(userId));
    }
    
    public Mono<NotificationPreferencesDto> updatePreferences(NotificationPreferencesRequest request) {
        return preferencesRepository.findByUserId(request.getUserId())
                .flatMap(existing -> {
                    updateFields(existing, request);
                    existing.setUpdatedAt(Instant.now());
                    return preferencesRepository.save(existing);
                })
                .switchIfEmpty(
                    Mono.defer(() -> {
                        UserNotificationPreferences newPrefs = createFromRequest(request);
                        return preferencesRepository.save(newPrefs);
                    })
                )
                .map(this::toDto);
    }
    
    private Mono<NotificationPreferencesDto> createDefaultPreferences(UUID userId) {
        UserNotificationPreferences defaults = UserNotificationPreferences.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .emailEnabled(true)
                .smsEnabled(true)
                .pushEnabled(true)
                .marketingEmail(false)
                .marketingSms(false)
                .reminder24h(true)
                .reminder1h(true)
                .reminder15min(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        return preferencesRepository.save(defaults)
                .map(this::toDto);
    }
    
    private UserNotificationPreferences createFromRequest(NotificationPreferencesRequest request) {
        return UserNotificationPreferences.builder()
                .id(UUID.randomUUID())
                .userId(request.getUserId())
                .emailEnabled(request.getEmailEnabled() != null ? request.getEmailEnabled() : true)
                .smsEnabled(request.getSmsEnabled() != null ? request.getSmsEnabled() : true)
                .pushEnabled(request.getPushEnabled() != null ? request.getPushEnabled() : true)
                .marketingEmail(request.getMarketingEmail() != null ? request.getMarketingEmail() : false)
                .marketingSms(request.getMarketingSms() != null ? request.getMarketingSms() : false)
                .reminder24h(request.getReminder24h() != null ? request.getReminder24h() : true)
                .reminder1h(request.getReminder1h() != null ? request.getReminder1h() : true)
                .reminder15min(request.getReminder15min() != null ? request.getReminder15min() : true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
    
    private void updateFields(UserNotificationPreferences existing, NotificationPreferencesRequest request) {
        if (request.getEmailEnabled() != null) {
            existing.setEmailEnabled(request.getEmailEnabled());
        }
        if (request.getSmsEnabled() != null) {
            existing.setSmsEnabled(request.getSmsEnabled());
        }
        if (request.getPushEnabled() != null) {
            existing.setPushEnabled(request.getPushEnabled());
        }
        if (request.getMarketingEmail() != null) {
            existing.setMarketingEmail(request.getMarketingEmail());
        }
        if (request.getMarketingSms() != null) {
            existing.setMarketingSms(request.getMarketingSms());
        }
        if (request.getReminder24h() != null) {
            existing.setReminder24h(request.getReminder24h());
        }
        if (request.getReminder1h() != null) {
            existing.setReminder1h(request.getReminder1h());
        }
        if (request.getReminder15min() != null) {
            existing.setReminder15min(request.getReminder15min());
        }
    }
    
    private NotificationPreferencesDto toDto(UserNotificationPreferences prefs) {
        return NotificationPreferencesDto.builder()
                .id(prefs.getId())
                .userId(prefs.getUserId())
                .emailEnabled(prefs.getEmailEnabled())
                .smsEnabled(prefs.getSmsEnabled())
                .pushEnabled(prefs.getPushEnabled())
                .marketingEmail(prefs.getMarketingEmail())
                .marketingSms(prefs.getMarketingSms())
                .reminder24h(prefs.getReminder24h())
                .reminder1h(prefs.getReminder1h())
                .reminder15min(prefs.getReminder15min())
                .updatedAt(prefs.getUpdatedAt())
                .build();
    }
}
