package com.healthapp.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationRequest {
    
    @NotBlank(message = "Cancellation reason is required")
    private String reason;
    
    private boolean requestRefund;
}
