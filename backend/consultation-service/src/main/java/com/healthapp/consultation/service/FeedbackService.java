package com.healthapp.consultation.service;

import com.healthapp.consultation.domain.ConsultationFeedback;
import com.healthapp.consultation.dto.SubmitFeedbackRequest;
import com.healthapp.consultation.repository.ConsultationFeedbackRepository;
import com.healthapp.consultation.repository.ConsultationSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for handling consultation feedback.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {
    
    private final ConsultationFeedbackRepository feedbackRepository;
    private final ConsultationSessionRepository sessionRepository;
    
    /**
     * Submits feedback for a consultation.
     */
    public Mono<ConsultationFeedback> submitFeedback(UUID patientId, SubmitFeedbackRequest request) {
        return sessionRepository.findById(request.getSessionId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Session not found")))
                .flatMap(session -> {
                    // Verify the patient is the one submitting feedback
                    if (!session.getPatientId().equals(patientId)) {
                        return Mono.error(new IllegalArgumentException("Only the patient can submit feedback"));
                    }
                    
                    ConsultationFeedback feedback = ConsultationFeedback.builder()
                            .sessionId(session.getId())
                            .patientId(patientId)
                            .doctorId(session.getDoctorId())
                            .overallRating(request.getOverallRating())
                            .videoQualityRating(request.getVideoQualityRating())
                            .audioQualityRating(request.getAudioQualityRating())
                            .doctorRating(request.getDoctorRating())
                            .reviewText(request.getReviewText())
                            .wouldRecommend(request.getWouldRecommend())
                            .hadTechnicalIssues(request.getHadTechnicalIssues())
                            .technicalIssueDescription(request.getTechnicalIssueDescription())
                            .build();
                    
                    return feedbackRepository.save(feedback);
                })
                .doOnSuccess(feedback -> log.info("Submitted feedback for session: {}", request.getSessionId()));
    }
    
    /**
     * Gets feedback for a session.
     */
    public Mono<ConsultationFeedback> getFeedback(UUID sessionId) {
        return feedbackRepository.findBySessionId(sessionId);
    }
    
    /**
     * Gets all feedback for a doctor.
     */
    public Flux<ConsultationFeedback> getDoctorFeedback(UUID doctorId) {
        return feedbackRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
    }
    
    /**
     * Gets average rating for a doctor.
     */
    public Mono<DoctorRatingSummary> getDoctorRatingSummary(UUID doctorId) {
        return Mono.zip(
                feedbackRepository.getAverageRatingByDoctor(doctorId).defaultIfEmpty(0.0),
                feedbackRepository.getAverageVideoQualityByDoctor(doctorId).defaultIfEmpty(0.0),
                feedbackRepository.countByDoctor(doctorId).defaultIfEmpty(0L)
        ).map(tuple -> DoctorRatingSummary.builder()
                .doctorId(doctorId)
                .averageOverallRating(tuple.getT1())
                .averageVideoQualityRating(tuple.getT2())
                .totalReviews(tuple.getT3())
                .build());
    }
    
    /**
     * Doctor rating summary.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DoctorRatingSummary {
        private UUID doctorId;
        private Double averageOverallRating;
        private Double averageVideoQualityRating;
        private Long totalReviews;
    }
}
