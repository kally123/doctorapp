package com.healthapp.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitReviewRequest {

    @NotNull(message = "Consultation ID is required")
    private UUID consultationId;

    @NotNull(message = "Overall rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer overallRating;

    @Min(value = 1, message = "Wait time rating must be at least 1")
    @Max(value = 5, message = "Wait time rating must be at most 5")
    private Integer waitTimeRating;

    @Min(value = 1, message = "Bedside manner rating must be at least 1")
    @Max(value = 5, message = "Bedside manner rating must be at most 5")
    private Integer bedsideMannerRating;

    @Min(value = 1, message = "Explanation rating must be at least 1")
    @Max(value = 5, message = "Explanation rating must be at most 5")
    private Integer explanationRating;

    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @Size(max = 2000, message = "Review text must be at most 2000 characters")
    private String reviewText;

    private List<String> positiveTags;

    private List<String> improvementTags;

    private boolean anonymous;
}
