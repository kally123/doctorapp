package com.healthapp.review.dto;

import com.healthapp.review.model.enums.VoteType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {

    @NotNull(message = "Vote type is required")
    private VoteType voteType;
}
