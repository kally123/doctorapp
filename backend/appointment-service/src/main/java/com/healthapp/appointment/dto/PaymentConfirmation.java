package com.healthapp.appointment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmation {
    
    @NotNull(message = "Payment ID is required")
    private UUID paymentId;
    
    @NotNull(message = "Payment status is required")
    private String status;
    
    private String gatewayPaymentId;
    private String gatewayOrderId;
}
