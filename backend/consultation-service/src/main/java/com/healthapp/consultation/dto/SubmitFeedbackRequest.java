package com.healthapp.consultation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request to submit feedback for a consultation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitFeedbackRequest {
    
    @NotNull(message = "Session ID is required")
    private UUID sessionId;
    
    @NotNull(message = "Overall rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer overallRating;
    
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer videoQualityRating;
    
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer audioQualityRating;
    
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer doctorRating;
    
    private String reviewText;
    
    private Boolean wouldRecommend;
    
    private Boolean hadTechnicalIssues;
    
    private String technicalIssueDescription;
}
