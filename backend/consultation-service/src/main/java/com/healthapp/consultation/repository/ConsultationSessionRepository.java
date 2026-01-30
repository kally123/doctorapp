package com.healthapp.consultation.repository;

import com.healthapp.consultation.domain.ConsultationSession;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface ConsultationSessionRepository extends ReactiveCrudRepository<ConsultationSession, UUID> {
    
    Mono<ConsultationSession> findByAppointmentId(UUID appointmentId);
    
    Mono<ConsultationSession> findByRoomName(String roomName);
    
    Flux<ConsultationSession> findByPatientIdOrderByScheduledStartTimeDesc(UUID patientId);
    
    Flux<ConsultationSession> findByDoctorIdOrderByScheduledStartTimeDesc(UUID doctorId);
    
    Flux<ConsultationSession> findByDoctorIdAndStatus(UUID doctorId, String status);
    
    Flux<ConsultationSession> findByPatientIdAndStatus(UUID patientId, String status);
    
    @Query("SELECT * FROM consultation_sessions WHERE doctor_id = :doctorId AND status = 'WAITING' ORDER BY scheduled_start_time")
    Flux<ConsultationSession> findWaitingPatientsByDoctor(UUID doctorId);
    
    @Query("SELECT * FROM consultation_sessions WHERE status = 'IN_PROGRESS' AND actual_start_time < :threshold")
    Flux<ConsultationSession> findOverdueSessions(Instant threshold);
    
    @Query("SELECT * FROM consultation_sessions WHERE doctor_id = :doctorId AND scheduled_start_time BETWEEN :start AND :end ORDER BY scheduled_start_time")
    Flux<ConsultationSession> findByDoctorAndDateRange(UUID doctorId, Instant start, Instant end);
    
    @Query("SELECT * FROM consultation_sessions WHERE patient_id = :patientId AND scheduled_start_time BETWEEN :start AND :end ORDER BY scheduled_start_time")
    Flux<ConsultationSession> findByPatientAndDateRange(UUID patientId, Instant start, Instant end);
    
    @Query("SELECT COUNT(*) FROM consultation_sessions WHERE doctor_id = :doctorId AND status = 'COMPLETED' AND actual_end_time > :since")
    Mono<Long> countCompletedSessionsSince(UUID doctorId, Instant since);
    
    @Query("SELECT AVG(total_duration_seconds) FROM consultation_sessions WHERE doctor_id = :doctorId AND status = 'COMPLETED'")
    Mono<Double> getAverageDurationByDoctor(UUID doctorId);
}
