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
public class RefundEvent {
    private String eventType;
    private UUID refundId;
    private UUID paymentId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String reason;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
