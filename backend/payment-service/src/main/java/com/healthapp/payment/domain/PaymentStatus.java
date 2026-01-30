package com.healthapp.payment.domain;

/**
 * Enum representing payment statuses
 */
public enum PaymentStatus {
    INITIATED,
    PENDING,
    AUTHORIZED,
    CAPTURED,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUND_INITIATED,
    PARTIALLY_REFUNDED,
    REFUNDED
}
