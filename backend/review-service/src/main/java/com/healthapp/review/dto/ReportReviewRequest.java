package com.healthapp.review.dto;

import com.healthapp.review.model.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportReviewRequest {

    @NotNull(message = "Report reason is required")
    private ReportReason reason;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;
}
