package com.healthapp.notification.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthapp.notification.domain.NotificationChannel;
import com.healthapp.notification.domain.NotificationStatus;
import com.healthapp.notification.domain.NotificationType;
import com.healthapp.notification.domain.ScheduledNotification;
import com.healthapp.notification.dto.NotificationRequest;
import com.healthapp.notification.repository.ScheduledNotificationRepository;
import com.healthapp.notification.repository.UserNotificationPreferencesRepository;
import com.healthapp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppointmentEventConsumer {
    
    private final NotificationService notificationService;
    private final ScheduledNotificationRepository scheduledRepository;
    private final UserNotificationPreferencesRepository preferencesRepository;
    private final ObjectMapper objectMapper;
    
    @Value("${notification.reminders.enabled:true}")
    private boolean remindersEnabled;
    
    @KafkaListener(topics = "appointment-events", groupId = "notification-service")
    public void handleAppointmentEvent(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();
            
            log.info("Received appointment event: {}", eventType);
            
            switch (eventType) {
                case "APPOINTMENT_CONFIRMED":
                    handleAppointmentConfirmed(event);
                    break;
                case "APPOINTMENT_CANCELLED":
                    handleAppointmentCancelled(event);
                    break;
                case "APPOINTMENT_RESCHEDULED":
                    handleAppointmentRescheduled(event);
                    break;
                default:
                    log.debug("Ignoring appointment event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing appointment event", e);
        }
    }
    
    private void handleAppointmentConfirmed(JsonNode event) {
        try {
            UUID appointmentId = UUID.fromString(event.get("appointmentId").asText());
            UUID patientId = UUID.fromString(event.get("patientId").asText());
            UUID doctorId = UUID.fromString(event.get("doctorId").asText());
            Instant scheduledTime = Instant.parse(event.get("scheduledTime").asText());
            String doctorName = event.has("doctorName") ? event.get("doctorName").asText() : "Doctor";
            String patientName = event.has("patientName") ? event.get("patientName").asText() : "Patient";
            String consultationType = event.has("consultationType") ? event.get("consultationType").asText() : "VIDEO";
            
            Map<String, Object> context = new HashMap<>();
            context.put("appointmentId", appointmentId.toString());
            context.put("doctorName", doctorName);
            context.put("patientName", patientName);
            context.put("scheduledTime", scheduledTime.toString());
            context.put("consultationType", consultationType);
            
            // Send confirmation to patient
            sendNotification(patientId, NotificationType.BOOKING_CONFIRMED, context, "APPOINTMENT", appointmentId.toString())
                    .subscribe();
            
            // Schedule reminders for patient
            if (remindersEnabled) {
                scheduleReminders(patientId, appointmentId, scheduledTime, context);
            }
            
        } catch (Exception e) {
            log.error("Error handling appointment confirmed event", e);
        }
    }
    
    private void handleAppointmentCancelled(JsonNode event) {
        try {
            UUID appointmentId = UUID.fromString(event.get("appointmentId").asText());
            UUID patientId = UUID.fromString(event.get("patientId").asText());
            String cancelledBy = event.has("cancelledBy") ? event.get("cancelledBy").asText() : "UNKNOWN";
            String reason = event.has("reason") ? event.get("reason").asText() : "";
            String doctorName = event.has("doctorName") ? event.get("doctorName").asText() : "Doctor";
            
            Map<String, Object> context = new HashMap<>();
            context.put("appointmentId", appointmentId.toString());
            context.put("cancelledBy", cancelledBy);
            context.put("reason", reason);
            context.put("doctorName", doctorName);
            
            // Send cancellation notification
            sendNotification(patientId, NotificationType.APPOINTMENT_CANCELLED, context, "APPOINTMENT", appointmentId.toString())
                    .subscribe();
            
            // Cancel scheduled reminders
            notificationService.cancelScheduledNotifications("APPOINTMENT", appointmentId.toString())
                    .subscribe();
            
        } catch (Exception e) {
            log.error("Error handling appointment cancelled event", e);
        }
    }
    
    private void handleAppointmentRescheduled(JsonNode event) {
        try {
            UUID appointmentId = UUID.fromString(event.get("appointmentId").asText());
            UUID patientId = UUID.fromString(event.get("patientId").asText());
            Instant newScheduledTime = Instant.parse(event.get("newScheduledTime").asText());
            Instant oldScheduledTime = Instant.parse(event.get("oldScheduledTime").asText());
            String doctorName = event.has("doctorName") ? event.get("doctorName").asText() : "Doctor";
            
            Map<String, Object> context = new HashMap<>();
            context.put("appointmentId", appointmentId.toString());
            context.put("doctorName", doctorName);
            context.put("newScheduledTime", newScheduledTime.toString());
            context.put("oldScheduledTime", oldScheduledTime.toString());
            
            // Send reschedule notification
            sendNotification(patientId, NotificationType.APPOINTMENT_RESCHEDULED, context, "APPOINTMENT", appointmentId.toString())
                    .subscribe();
            
            // Cancel old reminders and schedule new ones
            notificationService.cancelScheduledNotifications("APPOINTMENT", appointmentId.toString())
                    .then(Mono.fromRunnable(() -> {
                        if (remindersEnabled) {
                            scheduleReminders(patientId, appointmentId, newScheduledTime, context);
                        }
                    }))
                    .subscribe();
            
        } catch (Exception e) {
            log.error("Error handling appointment rescheduled event", e);
        }
    }
    
    private void scheduleReminders(UUID patientId, UUID appointmentId, Instant scheduledTime, Map<String, Object> context) {
        preferencesRepository.findByUserId(patientId)
                .defaultIfEmpty(createDefaultPreferences(patientId))
                .flatMap(prefs -> {
                    // Schedule 24-hour reminder
                    if (prefs.getReminder24h() != null && prefs.getReminder24h()) {
                        Instant reminder24h = scheduledTime.minus(24, ChronoUnit.HOURS);
                        if (reminder24h.isAfter(Instant.now())) {
                            scheduleReminder(patientId, appointmentId, reminder24h, NotificationType.APPOINTMENT_REMINDER, context);
                        }
                    }
                    
                    // Schedule 1-hour reminder
                    if (prefs.getReminder1h() != null && prefs.getReminder1h()) {
                        Instant reminder1h = scheduledTime.minus(1, ChronoUnit.HOURS);
                        if (reminder1h.isAfter(Instant.now())) {
                            scheduleReminder(patientId, appointmentId, reminder1h, NotificationType.APPOINTMENT_REMINDER, context);
                        }
                    }
                    
                    // Schedule 15-minute reminder
                    if (prefs.getReminder15min() != null && prefs.getReminder15min()) {
                        Instant reminder15min = scheduledTime.minus(15, ChronoUnit.MINUTES);
                        if (reminder15min.isAfter(Instant.now())) {
                            scheduleReminder(patientId, appointmentId, reminder15min, NotificationType.APPOINTMENT_REMINDER, context);
                        }
                    }
                    
                    return Mono.empty();
                })
                .subscribe();
    }
    
    private void scheduleReminder(UUID userId, UUID appointmentId, Instant scheduledFor, 
                                   NotificationType type, Map<String, Object> context) {
        for (NotificationChannel channel : new NotificationChannel[]{NotificationChannel.EMAIL, NotificationChannel.PUSH}) {
            ScheduledNotification scheduled = ScheduledNotification.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .notificationType(type.name())
                    .channel(channel.name())
                    .referenceType("APPOINTMENT")
                    .referenceId(appointmentId.toString())
                    .scheduledFor(scheduledFor)
                    .status(NotificationStatus.PENDING.name())
                    .context(serializeContext(context))
                    .createdAt(Instant.now())
                    .build();
            
            scheduledRepository.save(scheduled)
                    .doOnSuccess(s -> log.info("Scheduled {} reminder for appointment {} at {}", 
                            channel, appointmentId, scheduledFor))
                    .subscribe();
        }
    }
    
    private Mono<Void> sendNotification(UUID userId, NotificationType type, Map<String, Object> context, 
                                         String referenceType, String referenceId) {
        return notificationService.sendMultiChannel(userId, type, context, referenceType, referenceId)
                .then();
    }
    
    private String serializeContext(Map<String, Object> context) {
        try {
            return objectMapper.writeValueAsString(context);
        } catch (Exception e) {
            return null;
        }
    }
    
    private com.healthapp.notification.domain.UserNotificationPreferences createDefaultPreferences(UUID userId) {
        return com.healthapp.notification.domain.UserNotificationPreferences.builder()
                .userId(userId)
                .emailEnabled(true)
                .smsEnabled(true)
                .pushEnabled(true)
                .reminder24h(true)
                .reminder1h(true)
                .reminder15min(true)
                .build();
    }
}
