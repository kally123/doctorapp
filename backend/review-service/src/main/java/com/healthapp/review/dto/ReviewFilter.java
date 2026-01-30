package com.healthapp.review.dto;

import com.healthapp.review.model.enums.ConsultationType;
import com.healthapp.review.model.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFilter {

    private Integer minRating;
    private Integer maxRating;
    private ConsultationType consultationType;
    private ReviewStatus status;
    private Boolean hasResponse;
    private String sortBy; // "recent", "rating_high", "rating_low", "helpful"
}
