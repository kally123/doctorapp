package com.healthapp.consultation.controller;

import org.springframework.context.annotation.Profile;
import com.healthapp.consultation.dto.*;
import com.healthapp.consultation.service.ConsultationSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for consultation session management.
 */
@Slf4j
@Profile("!test")
@RestController
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
@Tag(name = "Consultation", description = "Consultation session management APIs")
public class ConsultationController {
    
    private final ConsultationSessionService sessionService;
    
    @PostMapping
    @Operation(summary = "Create a consultation session", description = "Creates a new video consultation session for an appointment")
    public Mono<ResponseEntity<SessionResponse>> createSession(@Valid @RequestBody CreateSessionRequest request) {
        return sessionService.createSession(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .doOnSuccess(r -> log.info("Created session for appointment: {}", request.getAppointmentId()));
    }
    
    @GetMapping("/{sessionId}")
    @Operation(summary = "Get session details", description = "Gets consultation session by ID")
    public Mono<ResponseEntity<SessionResponse>> getSession(@PathVariable UUID sessionId) {
        return sessionService.getSession(sessionId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Get session by appointment", description = "Gets consultation session by appointment ID")
    public Mono<ResponseEntity<SessionResponse>> getSessionByAppointment(@PathVariable UUID appointmentId) {
        return sessionService.getSessionByAppointment(appointmentId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{sessionId}/join")
    @Operation(summary = "Join a session", description = "Joins a consultation session and returns video access token")
    public Mono<ResponseEntity<VideoTokenResponse>> joinSession(
            @PathVariable UUID sessionId,
            @Valid @RequestBody JoinSessionRequest request) {
        return sessionService.joinSession(sessionId, request)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("User {} joined session {}", request.getUserId(), sessionId));
    }
    
    @PostMapping("/{sessionId}/leave")
    @Operation(summary = "Leave a session", description = "Leaves a consultation session")
    public Mono<ResponseEntity<Void>> leaveSession(
            @PathVariable UUID sessionId,
            @RequestParam UUID userId) {
        return sessionService.leaveSession(sessionId, userId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
    
    @PostMapping("/{sessionId}/end")
    @Operation(summary = "End a session", description = "Ends a consultation session")
    public Mono<ResponseEntity<SessionResponse>> endSession(
            @PathVariable UUID sessionId,
            @RequestParam(defaultValue = "completed") String reason) {
        return sessionService.endSession(sessionId, reason)
                .map(ResponseEntity::ok);
    }
    
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get doctor sessions", description = "Gets all consultation sessions for a doctor")
    public Flux<SessionResponse> getDoctorSessions(@PathVariable UUID doctorId) {
        return sessionService.getDoctorSessions(doctorId);
    }
    
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get patient sessions", description = "Gets all consultation sessions for a patient")
    public Flux<SessionResponse> getPatientSessions(@PathVariable UUID patientId) {
        return sessionService.getPatientSessions(patientId);
    }
    
    @GetMapping("/doctor/{doctorId}/waiting")
    @Operation(summary = "Get waiting patients", description = "Gets patients waiting for consultation with a doctor")
    public Flux<SessionResponse> getWaitingPatients(@PathVariable UUID doctorId) {
        return sessionService.getWaitingPatients(doctorId);
    }
    
    @PostMapping("/{sessionId}/token/refresh")
    @Operation(summary = "Refresh video token", description = "Refreshes the video access token for a session")
    public Mono<ResponseEntity<VideoTokenResponse>> refreshToken(
            @PathVariable UUID sessionId,
            @Valid @RequestBody JoinSessionRequest request) {
        // Reuse join session which generates a new token
        return sessionService.joinSession(sessionId, request)
                .map(ResponseEntity::ok);
    }
}
