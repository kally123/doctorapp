package com.healthapp.review.dto;

import com.healthapp.review.model.enums.ConsultationType;
import com.healthapp.review.model.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private UUID id;
    private UUID doctorId;
    private UUID patientId;
    private UUID consultationId;

    // Patient info (if not anonymous)
    private String patientName;
    private String patientAvatar;

    // Ratings
    private Integer overallRating;
    private Integer waitTimeRating;
    private Integer bedsideMannerRating;
    private Integer explanationRating;

    // Content
    private String title;
    private String reviewText;
    private ConsultationType consultationType;

    // Tags
    private List<String> positiveTags;
    private List<String> improvementTags;

    // Verification
    private Boolean isVerified;
    private Boolean isAnonymous;

    // Doctor response
    private String doctorResponse;
    private Instant doctorRespondedAt;

    // Engagement
    private Integer helpfulCount;
    private Integer notHelpfulCount;

    // User's vote (if authenticated)
    private String currentUserVote;

    // Status
    private ReviewStatus status;

    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
}
