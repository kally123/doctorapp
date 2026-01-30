package com.healthapp.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationRequest {
    
    @NotBlank(message = "Gateway order ID is required")
    private String gatewayOrderId;
    
    @NotBlank(message = "Gateway payment ID is required")
    private String gatewayPaymentId;
    
    @NotBlank(message = "Gateway signature is required")
    private String gatewaySignature;
}
