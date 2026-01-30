package com.healthapp.payment.dto;

import com.healthapp.payment.domain.PaymentStatus;
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
public class PaymentDto {
    private UUID id;
    private String orderType;
    private UUID orderId;
    private UUID userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private PaymentStatus status;
    private String gateway;
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private String failureReason;
    private BigDecimal refundedAmount;
    private String description;
    private Instant createdAt;
    private Instant completedAt;
}
