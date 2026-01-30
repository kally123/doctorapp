package com.healthapp.review.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("doctor_rating_aggregates")
public class DoctorRatingAggregate {

    @Id
    @Column("doctor_id")
    private UUID doctorId;

    @Column("average_rating")
    private BigDecimal averageRating;

    @Column("total_reviews")
    private Integer totalReviews;

    // Rating distribution
    @Column("five_star_count")
    private Integer fiveStarCount;

    @Column("four_star_count")
    private Integer fourStarCount;

    @Column("three_star_count")
    private Integer threeStarCount;

    @Column("two_star_count")
    private Integer twoStarCount;

    @Column("one_star_count")
    private Integer oneStarCount;

    // Detailed averages
    @Column("avg_wait_time_rating")
    private BigDecimal avgWaitTimeRating;

    @Column("avg_bedside_manner_rating")
    private BigDecimal avgBedsideMannerRating;

    @Column("avg_explanation_rating")
    private BigDecimal avgExplanationRating;

    // By consultation type
    @Column("video_consultation_rating")
    private BigDecimal videoConsultationRating;

    @Column("video_consultation_count")
    private Integer videoConsultationCount;

    @Column("in_person_rating")
    private BigDecimal inPersonRating;

    @Column("in_person_count")
    private Integer inPersonCount;

    // Top tags (stored as JSON string)
    @Column("top_positive_tags")
    private String topPositiveTags;

    @Column("top_improvement_tags")
    private String topImprovementTags;

    // Recommendation rate
    @Column("recommendation_rate")
    private BigDecimal recommendationRate;

    @LastModifiedDate
    @Column("last_updated")
    private Instant lastUpdated;

    public static DoctorRatingAggregate empty(UUID doctorId) {
        return DoctorRatingAggregate.builder()
                .doctorId(doctorId)
                .averageRating(BigDecimal.ZERO)
                .totalReviews(0)
                .fiveStarCount(0)
                .fourStarCount(0)
                .threeStarCount(0)
                .twoStarCount(0)
                .oneStarCount(0)
                .videoConsultationCount(0)
                .inPersonCount(0)
                .recommendationRate(BigDecimal.ZERO)
                .lastUpdated(Instant.now())
                .build();
    }
}
