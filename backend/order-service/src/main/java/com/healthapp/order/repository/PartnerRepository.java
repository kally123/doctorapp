package com.healthapp.order.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.domain.Partner;
import com.healthapp.order.domain.enums.PartnerType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Repository for Partner entity.
 */
@Profile("!test")
@Repository
public interface PartnerRepository extends R2dbcRepository<Partner, UUID> {

    Flux<Partner> findByPartnerTypeAndIsActiveTrue(PartnerType partnerType);

    Flux<Partner> findByPartnerTypeAndCityAndIsActiveTrue(PartnerType partnerType, String city);

    Flux<Partner> findByPartnerTypeAndIsActiveTrueAndIsVerifiedTrue(PartnerType partnerType);

    Mono<Partner> findByApiKeyHash(String apiKeyHash);

    @Query("""
        SELECT * FROM partners 
        WHERE partner_type = :partnerType 
        AND is_active = true 
        AND is_verified = true
        AND (
            :pincode = ANY(serviceable_pincodes) 
            OR cardinality(serviceable_pincodes) = 0
        )
        ORDER BY rating DESC
        """)
    Flux<Partner> findByPartnerTypeAndServiceablePincode(PartnerType partnerType, String pincode);

    @Query("""
        SELECT * FROM partners 
        WHERE partner_type = 'PHARMACY' 
        AND is_active = true 
        AND is_verified = true
        AND earth_distance(
            ll_to_earth(latitude, longitude),
            ll_to_earth(:lat, :lng)
        ) <= :radiusMeters
        ORDER BY rating DESC
        """)
    Flux<Partner> findNearbyPharmacies(BigDecimal lat, BigDecimal lng, double radiusMeters);

    @Query("""
        SELECT * FROM partners 
        WHERE partner_type = 'LAB' 
        AND is_active = true 
        AND is_verified = true
        AND (
            :pincode = ANY(serviceable_pincodes) 
            OR cardinality(serviceable_pincodes) = 0
        )
        ORDER BY rating DESC
        """)
    Flux<Partner> findLabsServicingPincode(String pincode);

    @Query("""
        SELECT * FROM partners 
        WHERE partner_type = :partnerType::text::partner_type
        AND is_active = true 
        AND is_verified = true
        ORDER BY rating DESC
        LIMIT :limit
        """)
    Flux<Partner> findNearbyPartners(PartnerType partnerType, double latitude, double longitude, double radiusKm, int limit);

    /**
     * Alias for findByPartnerTypeAndIsActiveTrue for compatibility.
     */
    default Flux<Partner> findByTypeAndIsActiveTrue(PartnerType partnerType) {
        return findByPartnerTypeAndIsActiveTrue(partnerType);
    }

    /**
     * Alias for findByPartnerTypeAndCityAndIsActiveTrue for compatibility.
     */
    default Flux<Partner> findByTypeAndCityAndIsActiveTrue(PartnerType partnerType, String city) {
        return findByPartnerTypeAndCityAndIsActiveTrue(partnerType, city);
    }
}
