package com.healthapp.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("refunds")
public class Refund {
    
    @Id
    private UUID id;
    
    @Column("payment_id")
    private UUID paymentId;
    
    @Column("amount")
    private BigDecimal amount;
    
    @Column("currency")
    private String currency;
    
    @Column("reason")
    private String reason;
    
    @Column("status")
    private String status;
    
    @Column("gateway_refund_id")
    private String gatewayRefundId;
    
    @Column("gateway_response")
    private String gatewayResponse;
    
    @Column("created_at")
    private Instant createdAt;
    
    @Column("processed_at")
    private Instant processedAt;
}
