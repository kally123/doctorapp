package com.healthapp.review.service;

import com.healthapp.review.model.entity.DoctorRatingAggregate;
import com.healthapp.review.model.enums.ConsultationType;
import com.healthapp.review.model.enums.ReviewStatus;
import com.healthapp.review.repository.RatingAggregateRepository;
import com.healthapp.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingAggregationService {

    private final ReviewRepository reviewRepository;
    private final RatingAggregateRepository aggregateRepository;

    /**
     * Update doctor rating aggregates after a new review is submitted or status changes
     */
    @Transactional
    public Mono<DoctorRatingAggregate> updateDoctorRatings(UUID doctorId) {
        log.info("Updating rating aggregates for doctor {}", doctorId);

        return Mono.zip(
                reviewRepository.getAverageRatingByDoctorId(doctorId).defaultIfEmpty(0.0),
                reviewRepository.countByDoctorIdAndStatusApproved(doctorId).defaultIfEmpty(0L),
                reviewRepository.countByDoctorIdAndRating(doctorId, 5).defaultIfEmpty(0),
                reviewRepository.countByDoctorIdAndRating(doctorId, 4).defaultIfEmpty(0),
                reviewRepository.countByDoctorIdAndRating(doctorId, 3).defaultIfEmpty(0),
                reviewRepository.countByDoctorIdAndRating(doctorId, 2).defaultIfEmpty(0),
                reviewRepository.countByDoctorIdAndRating(doctorId, 1).defaultIfEmpty(0)
        ).flatMap(tuple -> {
            double avgRating = tuple.getT1();
            long totalReviews = tuple.getT2();
            int fiveStars = tuple.getT3();
            int fourStars = tuple.getT4();
            int threeStars = tuple.getT5();
            int twoStars = tuple.getT6();
            int oneStars = tuple.getT7();

            // Calculate recommendation rate (4+ stars)
            long positiveReviews = fiveStars + fourStars;
            BigDecimal recommendationRate = totalReviews > 0 
                    ? BigDecimal.valueOf(positiveReviews * 100.0 / totalReviews)
                            .setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return aggregateRepository.findByDoctorId(doctorId)
                    .defaultIfEmpty(DoctorRatingAggregate.empty(doctorId))
                    .flatMap(aggregate -> {
                        aggregate.setAverageRating(BigDecimal.valueOf(avgRating)
                                .setScale(2, RoundingMode.HALF_UP));
                        aggregate.setTotalReviews((int) totalReviews);
                        aggregate.setFiveStarCount(fiveStars);
                        aggregate.setFourStarCount(fourStars);
                        aggregate.setThreeStarCount(threeStars);
                        aggregate.setTwoStarCount(twoStars);
                        aggregate.setOneStarCount(oneStars);
                        aggregate.setRecommendationRate(recommendationRate);
                        aggregate.setLastUpdated(Instant.now());
                        
                        return aggregateRepository.save(aggregate);
                    });
        }).doOnSuccess(aggregate -> 
                log.info("Updated ratings for doctor {}: avg={}, count={}", 
                        doctorId, aggregate.getAverageRating(), aggregate.getTotalReviews())
        );
    }

    /**
     * Update detailed category averages
     */
    @Transactional
    public Mono<Void> updateDetailedAverages(UUID doctorId) {
        return reviewRepository.findByDoctorIdAndStatusOrderByCreatedAtDesc(doctorId, ReviewStatus.APPROVED)
                .collectList()
                .flatMap(reviews -> {
                    if (reviews.isEmpty()) {
                        return Mono.empty();
                    }

                    double avgWaitTime = reviews.stream()
                            .filter(r -> r.getWaitTimeRating() != null)
                            .mapToInt(r -> r.getWaitTimeRating())
                            .average()
                            .orElse(0.0);

                    double avgBedside = reviews.stream()
                            .filter(r -> r.getBedsideMannerRating() != null)
                            .mapToInt(r -> r.getBedsideMannerRating())
                            .average()
                            .orElse(0.0);

                    double avgExplanation = reviews.stream()
                            .filter(r -> r.getExplanationRating() != null)
                            .mapToInt(r -> r.getExplanationRating())
                            .average()
                            .orElse(0.0);

                    // Video consultation stats
                    var videoReviews = reviews.stream()
                            .filter(r -> r.getConsultationType() == ConsultationType.VIDEO)
                            .toList();
                    double videoAvg = videoReviews.stream()
                            .mapToInt(r -> r.getOverallRating())
                            .average()
                            .orElse(0.0);

                    // In-person consultation stats
                    var inPersonReviews = reviews.stream()
                            .filter(r -> r.getConsultationType() == ConsultationType.IN_PERSON)
                            .toList();
                    double inPersonAvg = inPersonReviews.stream()
                            .mapToInt(r -> r.getOverallRating())
                            .average()
                            .orElse(0.0);

                    return aggregateRepository.findByDoctorId(doctorId)
                            .flatMap(aggregate -> {
                                aggregate.setAvgWaitTimeRating(toBigDecimal(avgWaitTime));
                                aggregate.setAvgBedsideMannerRating(toBigDecimal(avgBedside));
                                aggregate.setAvgExplanationRating(toBigDecimal(avgExplanation));
                                aggregate.setVideoConsultationRating(toBigDecimal(videoAvg));
                                aggregate.setVideoConsultationCount(videoReviews.size());
                                aggregate.setInPersonRating(toBigDecimal(inPersonAvg));
                                aggregate.setInPersonCount(inPersonReviews.size());
                                aggregate.setLastUpdated(Instant.now());
                                return aggregateRepository.save(aggregate);
                            });
                })
                .then();
    }

    private BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
