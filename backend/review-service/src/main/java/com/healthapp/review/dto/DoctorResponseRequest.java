package com.healthapp.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponseRequest {

    @NotBlank(message = "Response text is required")
    @Size(max = 1000, message = "Response must be at most 1000 characters")
    private String response;
}
