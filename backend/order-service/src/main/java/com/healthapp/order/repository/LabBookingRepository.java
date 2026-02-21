package com.healthapp.order.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.domain.LabBooking;
import com.healthapp.order.domain.enums.LabBookingStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Repository for LabBooking entity.
 */
@Profile("!test")
@Repository
public interface LabBookingRepository extends R2dbcRepository<LabBooking, UUID> {

    Mono<LabBooking> findByBookingNumber(String bookingNumber);

    Flux<LabBooking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Flux<LabBooking> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, LabBookingStatus status);

    Flux<LabBooking> findByLabPartnerIdOrderByCreatedAtDesc(UUID labPartnerId);

    Flux<LabBooking> findByLabPartnerIdAndStatusOrderByCreatedAtDesc(UUID labPartnerId, LabBookingStatus status);

    Flux<LabBooking> findByLabPartnerIdOrderByScheduledDateAsc(UUID labPartnerId);

    Flux<LabBooking> findByLabPartnerIdAndStatus(UUID labPartnerId, LabBookingStatus status);

    Flux<LabBooking> findByLabPartnerIdAndScheduledDate(UUID labPartnerId, LocalDate scheduledDate);

    @Query("SELECT * FROM lab_bookings WHERE user_id = :userId AND status NOT IN ('CANCELLED') ORDER BY created_at DESC LIMIT :limit")
    Flux<LabBooking> findRecentBookingsByUserId(UUID userId, int limit);

    Mono<Long> countByUserId(UUID userId);
}
