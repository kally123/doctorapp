package com.healthapp.order.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.domain.CollectionSlot;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Repository for CollectionSlot entity.
 */
@Profile("!test")
@Repository
public interface CollectionSlotRepository extends R2dbcRepository<CollectionSlot, UUID> {

    Flux<CollectionSlot> findByLabPartnerIdAndSlotDate(UUID labPartnerId, LocalDate slotDate);

    @Query("""
        SELECT * FROM collection_slots 
        WHERE lab_partner_id = :labPartnerId 
        AND slot_date BETWEEN :startDate AND :endDate
        AND is_available = true
        AND current_bookings < max_bookings
        ORDER BY slot_date ASC, start_time ASC
        """)
    Flux<CollectionSlot> findAvailableSlots(UUID labPartnerId, LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT * FROM collection_slots 
        WHERE slot_date BETWEEN :startDate AND :endDate
        AND is_available = true
        AND current_bookings < max_bookings
        AND (:pincode = ANY(serviceable_pincodes) OR cardinality(serviceable_pincodes) = 0)
        ORDER BY slot_date ASC, start_time ASC
        """)
    Flux<CollectionSlot> findAvailableSlotsForPincode(LocalDate startDate, LocalDate endDate, String pincode);

    @Query("UPDATE collection_slots SET current_bookings = current_bookings + 1 WHERE id = :slotId AND current_bookings < max_bookings")
    Mono<Integer> incrementBookingCount(UUID slotId);

    @Query("UPDATE collection_slots SET current_bookings = current_bookings - 1 WHERE id = :slotId AND current_bookings > 0")
    Mono<Integer> decrementBookingCount(UUID slotId);
}
