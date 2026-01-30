package com.healthapp.consultation.domain;

/**
 * Pricing model type for consultations.
 */
public enum PricingType {
    FLAT,        // Fixed fee regardless of duration
    PER_MINUTE,  // Charged per minute
    TIERED       // Different prices for duration tiers
}
