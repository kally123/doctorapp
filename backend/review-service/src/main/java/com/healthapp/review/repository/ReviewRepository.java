package com.healthapp.review.repository;

import com.healthapp.review.model.entity.DoctorReview;
import com.healthapp.review.model.enums.ConsultationType;
import com.healthapp.review.model.enums.ReviewStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ReviewRepository extends R2dbcRepository<DoctorReview, UUID> {

    Flux<DoctorReview> findByDoctorIdAndStatus(UUID doctorId, ReviewStatus status, Pageable pageable);

    Flux<DoctorReview> findByDoctorIdAndStatusOrderByCreatedAtDesc(UUID doctorId, ReviewStatus status);

    Flux<DoctorReview> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    Mono<DoctorReview> findByPatientIdAndConsultationId(UUID patientId, UUID consultationId);

    Mono<Boolean> existsByPatientIdAndConsultationId(UUID patientId, UUID consultationId);

    Mono<Boolean> existsByPatientIdAndDoctorIdAndConsultationId(UUID patientId, UUID doctorId, UUID consultationId);

    @Query("SELECT COUNT(*) FROM doctor_reviews WHERE doctor_id = :doctorId AND status = 'APPROVED'")
    Mono<Long> countByDoctorIdAndStatusApproved(UUID doctorId);

    @Query("SELECT AVG(overall_rating) FROM doctor_reviews WHERE doctor_id = :doctorId AND status = 'APPROVED'")
    Mono<Double> getAverageRatingByDoctorId(UUID doctorId);

    @Query("""
        SELECT * FROM doctor_reviews 
        WHERE doctor_id = :doctorId 
        AND status = :status 
        AND (:minRating IS NULL OR overall_rating >= :minRating)
        AND (:maxRating IS NULL OR overall_rating <= :maxRating)
        AND (:consultationType IS NULL OR consultation_type = :consultationType)
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<DoctorReview> findByDoctorWithFilter(
            UUID doctorId,
            ReviewStatus status,
            Integer minRating,
            Integer maxRating,
            String consultationType,
            int limit,
            int offset
    );

    @Query("SELECT COUNT(*) FROM doctor_reviews WHERE doctor_id = :doctorId AND status = 'APPROVED' AND overall_rating = :rating")
    Mono<Integer> countByDoctorIdAndRating(UUID doctorId, int rating);

    @Query("""
        SELECT consultation_type, AVG(overall_rating) as avg_rating, COUNT(*) as count 
        FROM doctor_reviews 
        WHERE doctor_id = :doctorId AND status = 'APPROVED' 
        GROUP BY consultation_type
        """)
    Flux<Object[]> getRatingsByConsultationType(UUID doctorId);

    Flux<DoctorReview> findByStatusOrderByCreatedAtDesc(ReviewStatus status, Pageable pageable);

    Mono<Long> countByStatus(ReviewStatus status);
}
