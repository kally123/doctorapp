package com.healthapp.review.service;

import com.healthapp.review.config.KafkaConfig;
import com.healthapp.review.model.entity.DoctorReview;
import com.healthapp.review.model.enums.ReviewStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReviewSubmitted(DoctorReview review) {
        ReviewEvent event = ReviewEvent.builder()
                .eventType("REVIEW_SUBMITTED")
                .reviewId(review.getId())
                .doctorId(review.getDoctorId())
                .patientId(review.getPatientId())
                .rating(review.getOverallRating())
                .status(review.getStatus())
                .timestamp(Instant.now())
                .build();

        kafkaTemplate.send(KafkaConfig.REVIEW_EVENTS_TOPIC, 
                review.getDoctorId().toString(), event);
        
        log.info("Published REVIEW_SUBMITTED event for review {}", review.getId());
    }

    public void publishDoctorResponded(UUID reviewId) {
        ReviewEvent event = ReviewEvent.builder()
                .eventType("DOCTOR_RESPONDED")
                .reviewId(reviewId)
                .timestamp(Instant.now())
                .build();

        kafkaTemplate.send(KafkaConfig.REVIEW_EVENTS_TOPIC, 
                reviewId.toString(), event);
        
        log.info("Published DOCTOR_RESPONDED event for review {}", reviewId);
    }

    public void publishReviewModerated(UUID reviewId, ReviewStatus newStatus) {
        ReviewEvent event = ReviewEvent.builder()
                .eventType("REVIEW_MODERATED")
                .reviewId(reviewId)
                .status(newStatus)
                .timestamp(Instant.now())
                .build();

        kafkaTemplate.send(KafkaConfig.REVIEW_EVENTS_TOPIC, 
                reviewId.toString(), event);
        
        log.info("Published REVIEW_MODERATED event for review {} with status {}", 
                reviewId, newStatus);
    }

    public void publishRatingUpdated(UUID doctorId, double averageRating, int totalReviews) {
        RatingUpdateEvent event = RatingUpdateEvent.builder()
                .eventType("RATING_UPDATED")
                .doctorId(doctorId)
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .timestamp(Instant.now())
                .build();

        kafkaTemplate.send(KafkaConfig.RATING_UPDATE_TOPIC, 
                doctorId.toString(), event);
        
        log.info("Published RATING_UPDATED event for doctor {}: avg={}, count={}", 
                doctorId, averageRating, totalReviews);
    }

    @lombok.Value
    @lombok.Builder
    public static class ReviewEvent {
        String eventType;
        UUID reviewId;
        UUID doctorId;
        UUID patientId;
        Integer rating;
        ReviewStatus status;
        Instant timestamp;
    }

    @lombok.Value
    @lombok.Builder
    public static class RatingUpdateEvent {
        String eventType;
        UUID doctorId;
        double averageRating;
        int totalReviews;
        Instant timestamp;
    }
}
