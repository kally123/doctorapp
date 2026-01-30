package com.healthapp.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiationResponse {
    private UUID paymentId;
    private String gatewayOrderId;
    private BigDecimal amount;
    private String currency;
    private String razorpayKeyId;
    private Map<String, String> prefill;
    private String checkoutUrl;
}
