package com.healthapp.consultation.service;

import com.healthapp.consultation.domain.*;
import com.healthapp.consultation.dto.*;
import com.healthapp.consultation.event.ConsultationEventPublisher;
import com.healthapp.consultation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Main service for managing consultation sessions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationSessionService {
    
    private final ConsultationSessionRepository sessionRepository;
    private final SessionParticipantRepository participantRepository;
    private final SessionEventRepository eventRepository;
    private final TwilioVideoService twilioVideoService;
    private final ConsultationEventPublisher eventPublisher;
    
    /**
     * Creates a new consultation session for an appointment.
     */
    public Mono<SessionResponse> createSession(CreateSessionRequest request) {
        String roomName = generateRoomName(request.getAppointmentId());
        
        return twilioVideoService.createRoom(roomName)
                .flatMap(roomInfo -> {
                    ConsultationSession session = ConsultationSession.builder()
                            .appointmentId(request.getAppointmentId())
                            .patientId(request.getPatientId())
                            .doctorId(request.getDoctorId())
                            .consultationMode(request.getConsultationMode().name())
                            .scheduledStartTime(request.getScheduledStartTime())
                            .scheduledDurationMinutes(request.getScheduledDurationMinutes())
                            .roomName(roomInfo.getRoomName())
                            .roomSid(roomInfo.getRoomSid())
                            .status(SessionStatus.SCHEDULED.name())
                            .isRecorded(false)
                            .build();
                    
                    return sessionRepository.save(session);
                })
                .flatMap(session -> {
                    eventPublisher.publishSessionCreated(session);
                    return Mono.just(mapToResponse(session));
                })
                .doOnSuccess(response -> log.info("Created consultation session: {} for appointment: {}", 
                        response.getId(), request.getAppointmentId()));
    }
    
    /**
     * Gets a session by ID.
     */
    public Mono<SessionResponse> getSession(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .map(this::mapToResponse)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Session not found: " + sessionId)));
    }
    
    /**
     * Gets a session by appointment ID.
     */
    public Mono<SessionResponse> getSessionByAppointment(UUID appointmentId) {
        return sessionRepository.findByAppointmentId(appointmentId)
                .map(this::mapToResponse)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Session not found for appointment: " + appointmentId)));
    }
    
    /**
     * Joins a session and returns video token.
     */
    public Mono<VideoTokenResponse> joinSession(UUID sessionId, JoinSessionRequest request) {
        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Session not found: " + sessionId)))
                .flatMap(session -> {
                    // Validate participant
                    if (request.getParticipantType() == ParticipantType.PATIENT 
                            && !session.getPatientId().equals(request.getUserId())) {
                        return Mono.error(new IllegalArgumentException("User is not the patient for this session"));
                    }
                    if (request.getParticipantType() == ParticipantType.DOCTOR 
                            && !session.getDoctorId().equals(request.getUserId())) {
                        return Mono.error(new IllegalArgumentException("User is not the doctor for this session"));
                    }
                    
                    // Create or update participant record
                    return participantRepository.findBySessionIdAndUserId(sessionId, request.getUserId())
                            .switchIfEmpty(Mono.defer(() -> {
                                SessionParticipant participant = SessionParticipant.builder()
                                        .sessionId(sessionId)
                                        .userId(request.getUserId())
                                        .participantType(request.getParticipantType().name())
                                        .joinedAt(Instant.now())
                                        .rejoinCount(0)
                                        .deviceType(request.getDeviceType())
                                        .browser(request.getBrowser())
                                        .os(request.getOs())
                                        .build();
                                return participantRepository.save(participant);
                            }))
                            .flatMap(participant -> {
                                // Update rejoin count if already exists
                                if (participant.getLeftAt() != null) {
                                    participant.setRejoinCount(participant.getRejoinCount() + 1);
                                    participant.setLeftAt(null);
                                    participant.setJoinedAt(Instant.now());
                                    return participantRepository.save(participant);
                                }
                                return Mono.just(participant);
                            })
                            .flatMap(participant -> updateSessionOnJoin(session, request.getParticipantType()))
                            .flatMap(updatedSession -> {
                                String identity = request.getParticipantType().name() + "_" + request.getUserId();
                                return twilioVideoService.generateAccessToken(identity, updatedSession.getRoomName());
                            });
                })
                .doOnSuccess(token -> log.info("User {} joined session {} as {}", 
                        request.getUserId(), sessionId, request.getParticipantType()));
    }
    
    /**
     * Updates session status when participant joins.
     */
    private Mono<ConsultationSession> updateSessionOnJoin(ConsultationSession session, ParticipantType participantType) {
        Instant now = Instant.now();
        boolean shouldStartSession = false;
        
        if (participantType == ParticipantType.PATIENT) {
            session.setPatientJoinedAt(now);
            if (SessionStatus.SCHEDULED.name().equals(session.getStatus())) {
                session.setStatus(SessionStatus.WAITING.name());
            }
        } else if (participantType == ParticipantType.DOCTOR) {
            session.setDoctorJoinedAt(now);
            // If patient already waiting, start the session
            if (session.getPatientJoinedAt() != null) {
                shouldStartSession = true;
            }
        }
        
        // Start session if both have joined
        if (shouldStartSession || (session.getPatientJoinedAt() != null && session.getDoctorJoinedAt() != null)) {
            session.setStatus(SessionStatus.IN_PROGRESS.name());
            session.setActualStartTime(now);
            return sessionRepository.save(session)
                    .doOnSuccess(s -> eventPublisher.publishSessionStarted(s));
        }
        
        return sessionRepository.save(session);
    }
    
    /**
     * Leaves a session.
     */
    public Mono<Void> leaveSession(UUID sessionId, UUID userId) {
        return participantRepository.findBySessionIdAndUserId(sessionId, userId)
                .flatMap(participant -> {
                    participant.setLeftAt(Instant.now());
                    return participantRepository.save(participant);
                })
                .then(checkAndEndSessionIfEmpty(sessionId));
    }
    
    /**
     * Ends a session.
     */
    public Mono<SessionResponse> endSession(UUID sessionId, String endReason) {
        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Session not found: " + sessionId)))
                .flatMap(session -> {
                    Instant now = Instant.now();
                    session.setStatus(SessionStatus.COMPLETED.name());
                    session.setActualEndTime(now);
                    session.setEndReason(endReason);
                    
                    // Calculate duration
                    if (session.getActualStartTime() != null) {
                        long durationSeconds = java.time.Duration.between(session.getActualStartTime(), now).getSeconds();
                        session.setTotalDurationSeconds((int) durationSeconds);
                    }
                    
                    return sessionRepository.save(session);
                })
                .flatMap(session -> {
                    // End the Twilio room
                    return twilioVideoService.endRoom(session.getRoomSid())
                            .then(Mono.just(session));
                })
                .flatMap(session -> {
                    eventPublisher.publishSessionEnded(session);
                    return Mono.just(mapToResponse(session));
                })
                .doOnSuccess(response -> log.info("Ended consultation session: {}", sessionId));
    }
    
    /**
     * Gets waiting patients for a doctor.
     */
    public Flux<SessionResponse> getWaitingPatients(UUID doctorId) {
        return sessionRepository.findWaitingPatientsByDoctor(doctorId)
                .map(this::mapToResponse);
    }
    
    /**
     * Gets sessions for a patient.
     */
    public Flux<SessionResponse> getPatientSessions(UUID patientId) {
        return sessionRepository.findByPatientIdOrderByScheduledStartTimeDesc(patientId)
                .map(this::mapToResponse);
    }
    
    /**
     * Gets sessions for a doctor.
     */
    public Flux<SessionResponse> getDoctorSessions(UUID doctorId) {
        return sessionRepository.findByDoctorIdOrderByScheduledStartTimeDesc(doctorId)
                .map(this::mapToResponse);
    }
    
    /**
     * Logs a session event.
     */
    public Mono<Void> logEvent(UUID sessionId, String eventType, UUID userId, String eventData) {
        SessionEvent event = SessionEvent.builder()
                .sessionId(sessionId)
                .eventType(eventType)
                .userId(userId)
                .eventData(eventData)
                .build();
        
        return eventRepository.save(event).then();
    }
    
    /**
     * Checks if session should be ended when empty.
     */
    private Mono<Void> checkAndEndSessionIfEmpty(UUID sessionId) {
        return participantRepository.findBySessionId(sessionId)
                .filter(p -> p.getLeftAt() == null) // Only active participants
                .hasElements()
                .flatMap(hasActiveParticipants -> {
                    if (!hasActiveParticipants) {
                        return endSession(sessionId, "all_participants_left").then();
                    }
                    return Mono.empty();
                });
    }
    
    private String generateRoomName(UUID appointmentId) {
        return "consultation_" + appointmentId.toString().replace("-", "");
    }
    
    private SessionResponse mapToResponse(ConsultationSession session) {
        return SessionResponse.builder()
                .id(session.getId())
                .appointmentId(session.getAppointmentId())
                .patientId(session.getPatientId())
                .doctorId(session.getDoctorId())
                .consultationMode(session.getConsultationModeEnum())
                .status(session.getStatusEnum())
                .scheduledStartTime(session.getScheduledStartTime())
                .scheduledDurationMinutes(session.getScheduledDurationMinutes())
                .roomName(session.getRoomName())
                .patientJoinedAt(session.getPatientJoinedAt())
                .doctorJoinedAt(session.getDoctorJoinedAt())
                .actualStartTime(session.getActualStartTime())
                .actualEndTime(session.getActualEndTime())
                .totalDurationSeconds(session.getTotalDurationSeconds())
                .patientConnectionQuality(session.getPatientConnectionQuality())
                .doctorConnectionQuality(session.getDoctorConnectionQuality())
                .isRecorded(session.getIsRecorded())
                .recordingUrl(session.getRecordingUrl())
                .endReason(session.getEndReason())
                .notes(session.getNotes())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
