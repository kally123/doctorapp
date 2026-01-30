package com.healthapp.consultation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Consultation pricing configuration per doctor and mode.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("consultation_pricing")
public class ConsultationPricing {
    
    @Id
    private UUID id;
    
    @Column("doctor_id")
    private UUID doctorId;
    
    @Column("consultation_mode")
    private String consultationMode;
    
    @Column("pricing_type")
    private String pricingType;
    
    // For FLAT pricing
    @Column("flat_fee")
    private BigDecimal flatFee;
    
    // For PER_MINUTE pricing
    @Column("per_minute_rate")
    private BigDecimal perMinuteRate;
    
    @Column("minimum_minutes")
    private Integer minimumMinutes;
    
    // For TIERED pricing (JSON)
    @Column("tier_config")
    private String tierConfig;
    
    @Column("currency")
    private String currency;
    
    @Column("is_active")
    private Boolean isActive;
    
    @CreatedDate
    @Column("created_at")
    private Instant createdAt;
    
    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;
    
    // Helper methods
    public ConsultationMode getConsultationModeEnum() {
        return consultationMode != null ? ConsultationMode.valueOf(consultationMode) : null;
    }
    
    public void setConsultationModeEnum(ConsultationMode mode) {
        this.consultationMode = mode != null ? mode.name() : null;
    }
    
    public PricingType getPricingTypeEnum() {
        return pricingType != null ? PricingType.valueOf(pricingType) : null;
    }
    
    public void setPricingTypeEnum(PricingType type) {
        this.pricingType = type != null ? type.name() : null;
    }
}
