package com.healthapp.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRatingResponse {

    private UUID doctorId;
    private BigDecimal averageRating;
    private Integer totalReviews;

    // Rating distribution
    private RatingDistribution ratingDistribution;

    // Detailed averages
    private BigDecimal avgWaitTimeRating;
    private BigDecimal avgBedsideMannerRating;
    private BigDecimal avgExplanationRating;

    // By consultation type
    private ConsultationTypeRating videoConsultation;
    private ConsultationTypeRating inPersonConsultation;

    // Top tags
    private List<TagCount> topPositiveTags;
    private List<TagCount> topImprovementTags;

    // Recommendation rate (percentage)
    private BigDecimal recommendationRate;

    private Instant lastUpdated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingDistribution {
        private Integer fiveStars;
        private Integer fourStars;
        private Integer threeStars;
        private Integer twoStars;
        private Integer oneStar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsultationTypeRating {
        private BigDecimal averageRating;
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagCount {
        private String tag;
        private Integer count;
    }
}
