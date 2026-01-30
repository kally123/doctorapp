package com.healthapp.payment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private String eventType;
    private UUID paymentId;
    private UUID orderId;
    private String orderType;
    private UUID userId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String gatewayPaymentId;
    private String failureReason;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
