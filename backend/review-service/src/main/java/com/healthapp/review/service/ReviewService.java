package com.healthapp.review.service;

import com.healthapp.review.dto.*;
import com.healthapp.review.model.entity.DoctorRatingAggregate;
import com.healthapp.review.model.entity.DoctorReview;
import com.healthapp.review.model.entity.ReviewVote;
import com.healthapp.review.model.enums.ConsultationType;
import com.healthapp.review.model.enums.ReviewStatus;
import com.healthapp.review.model.enums.VoteType;
import com.healthapp.review.repository.RatingAggregateRepository;
import com.healthapp.review.repository.ReviewRepository;
import com.healthapp.review.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RatingAggregateRepository aggregateRepository;
    private final VoteRepository voteRepository;
    private final RatingAggregationService ratingAggregationService;
    private final ReviewEventPublisher eventPublisher;

    @Value("${moderation.auto-approve.enabled:true}")
    private boolean autoApproveEnabled;

    @Value("${moderation.auto-approve.min-text-length:10}")
    private int minTextLength;

    @Value("${moderation.profanity-filter.enabled:true}")
    private boolean profanityFilterEnabled;

    // Basic profanity patterns (would use a proper library in production)
    private static final Pattern PROFANITY_PATTERN = Pattern.compile(
            "\\b(badword1|badword2)\\b", 
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Submit a new review
     */
    @Transactional
    public Mono<ReviewResponse> submitReview(UUID patientId, SubmitReviewRequest request) {
        log.info("Submitting review for consultation {} by patient {}", 
                request.getConsultationId(), patientId);

        // Check for existing review
        return reviewRepository.existsByPatientIdAndConsultationId(patientId, request.getConsultationId())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalStateException(
                                "Review already exists for this consultation"));
                    }
                    return createReview(patientId, request);
                });
    }

    private Mono<ReviewResponse> createReview(UUID patientId, SubmitReviewRequest request) {
        // In a real app, we would validate consultation exists and belongs to patient
        // For now, we'll assume the doctorId comes from the consultation lookup
        UUID doctorId = UUID.randomUUID(); // This would come from consultation service

        DoctorReview review = DoctorReview.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .consultationId(request.getConsultationId())
                .overallRating(request.getOverallRating())
                .waitTimeRating(request.getWaitTimeRating())
                .bedsideMannerRating(request.getBedsideMannerRating())
                .explanationRating(request.getExplanationRating())
                .title(sanitizeText(request.getTitle()))
                .reviewText(sanitizeText(request.getReviewText()))
                .consultationType(ConsultationType.VIDEO) // Would come from consultation
                .isVerified(true)
                .status(autoModerate(request) ? ReviewStatus.APPROVED : ReviewStatus.PENDING)
                .isAnonymous(request.isAnonymous())
                .helpfulCount(0)
                .notHelpfulCount(0)
                .reportCount(0)
                .build();

        review.setPositiveTagsList(request.getPositiveTags());
        review.setImprovementTagsList(request.getImprovementTags());

        return reviewRepository.save(review)
                .flatMap(savedReview -> {
                    // Update aggregates asynchronously
                    ratingAggregationService.updateDoctorRatings(savedReview.getDoctorId())
                            .subscribe();

                    // Publish event
                    eventPublisher.publishReviewSubmitted(savedReview);

                    return Mono.just(toResponse(savedReview));
                });
    }

    /**
     * Get reviews for a doctor
     */
    public Flux<ReviewResponse> getDoctorReviews(UUID doctorId, ReviewFilter filter, int page, int size) {
        return reviewRepository.findByDoctorWithFilter(
                        doctorId,
                        ReviewStatus.APPROVED,
                        filter != null ? filter.getMinRating() : null,
                        filter != null ? filter.getMaxRating() : null,
                        filter != null && filter.getConsultationType() != null ? 
                                filter.getConsultationType().name() : null,
                        size,
                        page * size
                )
                .map(this::toResponse);
    }

    /**
     * Get rating aggregate for doctor
     */
    public Mono<DoctorRatingResponse> getDoctorRating(UUID doctorId) {
        return aggregateRepository.findByDoctorId(doctorId)
                .defaultIfEmpty(DoctorRatingAggregate.empty(doctorId))
                .map(this::toRatingResponse);
    }

    /**
     * Doctor responds to review
     */
    @Transactional
    public Mono<ReviewResponse> respondToReview(UUID doctorId, UUID reviewId, DoctorResponseRequest request) {
        return reviewRepository.findById(reviewId)
                .filter(review -> review.getDoctorId().equals(doctorId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Review not found or unauthorized")))
                .filter(review -> review.getDoctorResponse() == null)
                .switchIfEmpty(Mono.error(new IllegalStateException("Already responded to this review")))
                .flatMap(review -> {
                    review.setDoctorResponse(sanitizeText(request.getResponse()));
                    review.setDoctorRespondedAt(Instant.now());
                    return reviewRepository.save(review);
                })
                .map(this::toResponse)
                .doOnSuccess(response -> eventPublisher.publishDoctorResponded(reviewId));
    }

    /**
     * Vote review as helpful/not helpful
     */
    @Transactional
    public Mono<Void> voteReview(UUID userId, UUID reviewId, VoteType voteType) {
        return voteRepository.findByReviewIdAndUserId(reviewId, userId)
                .flatMap(existingVote -> {
                    if (existingVote.getVoteType() == voteType) {
                        // Remove vote if same type
                        return voteRepository.delete(existingVote)
                                .then(updateVoteCounts(reviewId));
                    } else {
                        // Change vote type
                        existingVote.setVoteType(voteType);
                        return voteRepository.save(existingVote)
                                .then(updateVoteCounts(reviewId));
                    }
                })
                .switchIfEmpty(
                        // Create new vote
                        voteRepository.save(ReviewVote.builder()
                                        .reviewId(reviewId)
                                        .userId(userId)
                                        .voteType(voteType)
                                        .build())
                                .then(updateVoteCounts(reviewId))
                );
    }

    private Mono<Void> updateVoteCounts(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .flatMap(review -> 
                        Mono.zip(
                                voteRepository.countByReviewIdAndVoteType(reviewId, VoteType.HELPFUL),
                                voteRepository.countByReviewIdAndVoteType(reviewId, VoteType.NOT_HELPFUL)
                        ).flatMap(counts -> {
                            review.setHelpfulCount(counts.getT1());
                            review.setNotHelpfulCount(counts.getT2());
                            return reviewRepository.save(review);
                        })
                )
                .then();
    }

    /**
     * Get patient's reviews
     */
    public Flux<ReviewResponse> getPatientReviews(UUID patientId) {
        return reviewRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .map(this::toResponse);
    }

    /**
     * Get single review by ID
     */
    public Mono<ReviewResponse> getReviewById(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .map(this::toResponse);
    }

    /**
     * Auto-moderation logic
     */
    private boolean autoModerate(SubmitReviewRequest request) {
        if (!autoApproveEnabled) {
            return false;
        }

        // Check text length
        if (request.getReviewText() != null && request.getReviewText().length() < minTextLength) {
            return false;
        }

        // Check for profanity
        if (profanityFilterEnabled && containsProfanity(request.getReviewText())) {
            return false;
        }

        return true;
    }

    private boolean containsProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return PROFANITY_PATTERN.matcher(text).find();
    }

    private String sanitizeText(String text) {
        if (text == null) {
            return null;
        }
        // Basic HTML sanitization
        return text.replaceAll("<[^>]*>", "").trim();
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

    private DoctorRatingResponse toRatingResponse(DoctorRatingAggregate aggregate) {
        return DoctorRatingResponse.builder()
                .doctorId(aggregate.getDoctorId())
                .averageRating(aggregate.getAverageRating())
                .totalReviews(aggregate.getTotalReviews())
                .ratingDistribution(DoctorRatingResponse.RatingDistribution.builder()
                        .fiveStars(aggregate.getFiveStarCount())
                        .fourStars(aggregate.getFourStarCount())
                        .threeStars(aggregate.getThreeStarCount())
                        .twoStars(aggregate.getTwoStarCount())
                        .oneStar(aggregate.getOneStarCount())
                        .build())
                .avgWaitTimeRating(aggregate.getAvgWaitTimeRating())
                .avgBedsideMannerRating(aggregate.getAvgBedsideMannerRating())
                .avgExplanationRating(aggregate.getAvgExplanationRating())
                .videoConsultation(DoctorRatingResponse.ConsultationTypeRating.builder()
                        .averageRating(aggregate.getVideoConsultationRating())
                        .count(aggregate.getVideoConsultationCount())
                        .build())
                .inPersonConsultation(DoctorRatingResponse.ConsultationTypeRating.builder()
                        .averageRating(aggregate.getInPersonRating())
                        .count(aggregate.getInPersonCount())
                        .build())
                .recommendationRate(aggregate.getRecommendationRate())
                .lastUpdated(aggregate.getLastUpdated())
                .build();
    }
}
