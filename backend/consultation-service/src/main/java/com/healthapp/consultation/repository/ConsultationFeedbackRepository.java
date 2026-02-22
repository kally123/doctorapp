package com.healthapp.consultation.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.consultation.domain.ConsultationFeedback;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Profile("!test")
@Repository
public interface ConsultationFeedbackRepository extends ReactiveCrudRepository<ConsultationFeedback, UUID> {
    
    Mono<ConsultationFeedback> findBySessionId(UUID sessionId);
    
    Flux<ConsultationFeedback> findByDoctorIdOrderByCreatedAtDesc(UUID doctorId);
    
    Flux<ConsultationFeedback> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    
    @Query("SELECT AVG(overall_rating) FROM consultation_feedback WHERE doctor_id = :doctorId")
    Mono<Double> getAverageRatingByDoctor(UUID doctorId);
    
    @Query("SELECT AVG(video_quality_rating) FROM consultation_feedback WHERE doctor_id = :doctorId")
    Mono<Double> getAverageVideoQualityByDoctor(UUID doctorId);
    
    @Query("SELECT COUNT(*) FROM consultation_feedback WHERE doctor_id = :doctorId")
    Mono<Long> countByDoctor(UUID doctorId);
}
