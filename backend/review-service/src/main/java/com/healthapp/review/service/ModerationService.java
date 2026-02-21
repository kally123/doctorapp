package com.healthapp.review.service;

import org.springframework.context.annotation.Profile;
import com.healthapp.review.dto.ModerationRequest;
import com.healthapp.review.dto.ReportReviewRequest;
import com.healthapp.review.dto.ReviewResponse;
import com.healthapp.review.model.entity.DoctorReview;
import com.healthapp.review.model.entity.ReviewReport;
import com.healthapp.review.model.enums.ReportStatus;
import com.healthapp.review.model.enums.ReviewStatus;
import com.healthapp.review.repository.ReportRepository;
import com.healthapp.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Profile("!test")
@Service
@Slf4j
@RequiredArgsConstructor
public class ModerationService {

    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final RatingAggregationService ratingAggregationService;
    private final ReviewEventPublisher eventPublisher;

    /**
     * Report a review
     */
    @Transactional
    public Mono<Void> reportReview(UUID userId, UUID reviewId, ReportReviewRequest request) {
        // Check if user already reported this review
        return reportRepository.existsByReviewIdAndReporterId(reviewId, userId)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalStateException("Already reported this review"));
                    }

                    ReviewReport report = ReviewReport.builder()
                            .reviewId(reviewId)
                            .reporterId(userId)
                            .reason(request.getReason())
                            .description(request.getDescription())
                            .status(ReportStatus.PENDING)
                            .build();

                    return reportRepository.save(report)
                            .flatMap(savedReport -> incrementReportCount(reviewId))
                            .then();
                });
    }

    private Mono<DoctorReview> incrementReportCount(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .flatMap(review -> {
                    review.setReportCount(review.getReportCount() + 1);
                    
                    // Auto-flag if too many reports
                    if (review.getReportCount() >= 5 && review.getStatus() == ReviewStatus.APPROVED) {
                        review.setStatus(ReviewStatus.FLAGGED);
                    }
                    
                    return reviewRepository.save(review);
                });
    }

    /**
     * Get pending reviews for moderation
     */
    public Flux<ReviewResponse> getPendingReviews(int page, int size) {
        return reviewRepository.findByStatusOrderByCreatedAtDesc(
                        ReviewStatus.PENDING, 
                        PageRequest.of(page, size)
                )
                .map(this::toResponse);
    }

    /**
     * Get flagged reviews for moderation
     */
    public Flux<ReviewResponse> getFlaggedReviews(int page, int size) {
        return reviewRepository.findByStatusOrderByCreatedAtDesc(
                        ReviewStatus.FLAGGED, 
                        PageRequest.of(page, size)
                )
                .map(this::toResponse);
    }

    /**
     * Moderate a review (approve, reject, hide)
     */
    @Transactional
    public Mono<ReviewResponse> moderateReview(UUID moderatorId, UUID reviewId, ModerationRequest request) {
        return reviewRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Review not found")))
                .flatMap(review -> {
                    ReviewStatus previousStatus = review.getStatus();
                    
                    review.setStatus(request.getStatus());
                    review.setModerationNotes(request.getNotes());
                    review.setModeratedBy(moderatorId);
                    review.setModeratedAt(Instant.now());
                    
                    return reviewRepository.save(review)
                            .flatMap(savedReview -> {
                                // Update aggregates if status changed to/from APPROVED
                                if (previousStatus != request.getStatus() &&
                                        (previousStatus == ReviewStatus.APPROVED || 
                                         request.getStatus() == ReviewStatus.APPROVED)) {
                                    return ratingAggregationService
                                            .updateDoctorRatings(savedReview.getDoctorId())
                                            .thenReturn(savedReview);
                                }
                                return Mono.just(savedReview);
                            });
                })
                .map(this::toResponse)
                .doOnSuccess(response -> {
                    log.info("Review {} moderated to status {} by {}", 
                            reviewId, request.getStatus(), moderatorId);
                    eventPublisher.publishReviewModerated(reviewId, request.getStatus());
                });
    }

    /**
     * Get moderation stats
     */
    public Mono<ModerationStats> getModerationStats() {
        return Mono.zip(
                reviewRepository.countByStatus(ReviewStatus.PENDING),
                reviewRepository.countByStatus(ReviewStatus.FLAGGED),
                reportRepository.countByStatus(ReportStatus.PENDING)
        ).map(tuple -> ModerationStats.builder()
                .pendingReviews(tuple.getT1())
                .flaggedReviews(tuple.getT2())
                .pendingReports(tuple.getT3())
                .build());
    }

    private ReviewResponse toResponse(DoctorReview review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .doctorId(review.getDoctorId())
                .patientId(review.getPatientId())
                .consultationId(review.getConsultationId())
                .overallRating(review.getOverallRating())
                .waitTimeRating(review.getWaitTimeRating())
                .bedsideMannerRating(review.getBedsideMannerRating())
                .explanationRating(review.getExplanationRating())
                .title(review.getTitle())
                .reviewText(review.getReviewText())
                .consultationType(review.getConsultationType())
                .positiveTags(review.getPositiveTagsList())
                .improvementTags(review.getImprovementTagsList())
                .isVerified(review.getIsVerified())
                .isAnonymous(review.getIsAnonymous())
                .doctorResponse(review.getDoctorResponse())
                .doctorRespondedAt(review.getDoctorRespondedAt())
                .helpfulCount(review.getHelpfulCount())
                .notHelpfulCount(review.getNotHelpfulCount())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    @lombok.Value
    @lombok.Builder
    public static class ModerationStats {
        long pendingReviews;
        long flaggedReviews;
        long pendingReports;
    }
}
