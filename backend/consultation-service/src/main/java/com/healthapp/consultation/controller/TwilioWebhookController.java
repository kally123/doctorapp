package com.healthapp.consultation.controller;

import com.healthapp.consultation.service.ConsultationSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * Webhook controller for Twilio Video status callbacks.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/twilio")
@RequiredArgsConstructor
public class TwilioWebhookController {
    
    private final ConsultationSessionService sessionService;
    
    /**
     * Handles Twilio Video room status callbacks.
     */
    @PostMapping("/video/status")
    public Mono<ResponseEntity<Void>> handleVideoStatusCallback(@RequestBody Map<String, String> payload) {
        String statusCallback = payload.get("StatusCallbackEvent");
        String roomSid = payload.get("RoomSid");
        String roomName = payload.get("RoomName");
        
        log.info("Twilio callback - Event: {}, Room: {}, SID: {}", statusCallback, roomName, roomSid);
        
        return switch (statusCallback) {
            case "room-created" -> handleRoomCreated(roomName, roomSid);
            case "room-ended" -> handleRoomEnded(roomName, roomSid);
            case "participant-connected" -> handleParticipantConnected(payload);
            case "participant-disconnected" -> handleParticipantDisconnected(payload);
            case "recording-started" -> handleRecordingStarted(payload);
            case "recording-completed" -> handleRecordingCompleted(payload);
            default -> {
                log.debug("Ignoring Twilio callback event: {}", statusCallback);
                yield Mono.just(ResponseEntity.ok().build());
            }
        };
    }
    
    private Mono<ResponseEntity<Void>> handleRoomCreated(String roomName, String roomSid) {
        log.info("Room created: {} ({})", roomName, roomSid);
        return Mono.just(ResponseEntity.ok().build());
    }
    
    private Mono<ResponseEntity<Void>> handleRoomEnded(String roomName, String roomSid) {
        log.info("Room ended: {} ({})", roomName, roomSid);
        // Room ended externally - update session status if still active
        // This is handled by the session service when participants leave
        return Mono.just(ResponseEntity.ok().build());
    }
    
    private Mono<ResponseEntity<Void>> handleParticipantConnected(Map<String, String> payload) {
        String roomName = payload.get("RoomName");
        String participantIdentity = payload.get("ParticipantIdentity");
        
        log.info("Participant connected: {} in room: {}", participantIdentity, roomName);
        
        // Parse participant identity (format: PATIENT_uuid or DOCTOR_uuid)
        try {
            String[] parts = participantIdentity.split("_", 2);
            if (parts.length == 2) {
                UUID userId = UUID.fromString(parts[1]);
                // Log the event for analytics
                // sessionService.logEvent() can be called here
            }
        } catch (Exception e) {
            log.warn("Could not parse participant identity: {}", participantIdentity);
        }
        
        return Mono.just(ResponseEntity.ok().build());
    }
    
    private Mono<ResponseEntity<Void>> handleParticipantDisconnected(Map<String, String> payload) {
        String roomName = payload.get("RoomName");
        String participantIdentity = payload.get("ParticipantIdentity");
        String participantDuration = payload.get("ParticipantDuration");
        
        log.info("Participant disconnected: {} from room: {} (duration: {}s)", 
                participantIdentity, roomName, participantDuration);
        
        return Mono.just(ResponseEntity.ok().build());
    }
    
    private Mono<ResponseEntity<Void>> handleRecordingStarted(Map<String, String> payload) {
        String roomName = payload.get("RoomName");
        String recordingSid = payload.get("RecordingSid");
        
        log.info("Recording started in room: {} ({})", roomName, recordingSid);
        return Mono.just(ResponseEntity.ok().build());
    }
    
    private Mono<ResponseEntity<Void>> handleRecordingCompleted(Map<String, String> payload) {
        String roomName = payload.get("RoomName");
        String recordingSid = payload.get("RecordingSid");
        String recordingDuration = payload.get("RecordingDuration");
        
        log.info("Recording completed in room: {} ({}) - duration: {}s", 
                roomName, recordingSid, recordingDuration);
        
        // Update session with recording info if needed
        return Mono.just(ResponseEntity.ok().build());
    }
}
