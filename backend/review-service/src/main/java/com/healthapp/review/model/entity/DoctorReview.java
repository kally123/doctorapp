package com.healthapp.review.model.entity;

import com.healthapp.review.model.enums.ConsultationType;
import com.healthapp.review.model.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("doctor_reviews")
public class DoctorReview {

    @Id
    private UUID id;

    @Column("doctor_id")
    private UUID doctorId;

    @Column("patient_id")
    private UUID patientId;

    @Column("consultation_id")
    private UUID consultationId;

    @Column("appointment_id")
    private UUID appointmentId;

    // Ratings (1-5 scale)
    @Column("overall_rating")
    private Integer overallRating;

    @Column("wait_time_rating")
    private Integer waitTimeRating;

    @Column("bedside_manner_rating")
    private Integer bedsideMannerRating;

    @Column("explanation_rating")
    private Integer explanationRating;

    // Review content
    private String title;

    @Column("review_text")
    private String reviewText;

    @Column("consultation_type")
    private ConsultationType consultationType;

    // Tags stored as comma-separated for R2DBC compatibility
    @Column("positive_tags")
    private String positiveTags;

    @Column("improvement_tags")
    private String improvementTags;

    // Verification
    @Column("is_verified")
    private Boolean isVerified;

    // Moderation
    private ReviewStatus status;

    @Column("moderation_notes")
    private String moderationNotes;

    @Column("moderated_by")
    private UUID moderatedBy;

    @Column("moderated_at")
    private Instant moderatedAt;

    // Doctor response
    @Column("doctor_response")
    private String doctorResponse;

    @Column("doctor_responded_at")
    private Instant doctorRespondedAt;

    // Engagement
    @Column("helpful_count")
    private Integer helpfulCount;

    @Column("not_helpful_count")
    private Integer notHelpfulCount;

    @Column("report_count")
    private Integer reportCount;

    // Visibility
    @Column("is_anonymous")
    private Boolean isAnonymous;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    // Helper methods for tags
    public List<String> getPositiveTagsList() {
        if (positiveTags == null || positiveTags.isEmpty()) {
            return List.of();
        }
        return List.of(positiveTags.split(","));
    }

    public void setPositiveTagsList(List<String> tags) {
        this.positiveTags = tags != null ? String.join(",", tags) : null;
    }

    public List<String> getImprovementTagsList() {
        if (improvementTags == null || improvementTags.isEmpty()) {
            return List.of();
        }
        return List.of(improvementTags.split(","));
    }

    public void setImprovementTagsList(List<String> tags) {
        this.improvementTags = tags != null ? String.join(",", tags) : null;
    }
}
