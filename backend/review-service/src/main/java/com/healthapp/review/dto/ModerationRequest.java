package com.healthapp.review.dto;

import com.healthapp.review.model.enums.ReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationRequest {

    @NotNull(message = "Status is required")
    private ReviewStatus status;

    private String notes;
}
