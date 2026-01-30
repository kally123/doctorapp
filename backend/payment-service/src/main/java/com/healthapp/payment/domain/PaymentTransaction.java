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
@Table("payment_transactions")
public class PaymentTransaction {
    
    @Id
    private UUID id;
    
    @Column("order_type")
    private String orderType;
    
    @Column("order_id")
    private UUID orderId;
    
    @Column("user_id")
    private UUID userId;
    
    @Column("amount")
    private BigDecimal amount;
    
    @Column("currency")
    private String currency;
    
    @Column("payment_method")
    private String paymentMethod;
    
    @Column("status")
    private PaymentStatus status;
    
    @Column("gateway")
    private String gateway;
    
    @Column("gateway_order_id")
    private String gatewayOrderId;
    
    @Column("gateway_payment_id")
    private String gatewayPaymentId;
    
    @Column("gateway_signature")
    private String gatewaySignature;
    
    @Column("gateway_response")
    private String gatewayResponse;
    
    @Column("failure_reason")
    private String failureReason;
    
    @Column("refunded_amount")
    private BigDecimal refundedAmount;
    
    @Column("description")
    private String description;
    
    @Column("metadata")
    private String metadata;
    
    @Column("created_at")
    private Instant createdAt;
    
    @Column("updated_at")
    private Instant updatedAt;
    
    @Column("completed_at")
    private Instant completedAt;
    
    @Column("idempotency_key")
    private String idempotencyKey;
}
