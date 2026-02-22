package com.healthapp.order.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.domain.DeliveryAddress;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository for DeliveryAddress entity.
 */
@Profile("!test")
@Repository
public interface DeliveryAddressRepository extends R2dbcRepository<DeliveryAddress, UUID> {

    Flux<DeliveryAddress> findByUserIdAndIsActiveTrue(UUID userId);

    Mono<DeliveryAddress> findByUserIdAndIsDefaultTrue(UUID userId);

    @Query("UPDATE delivery_addresses SET is_default = false WHERE user_id = :userId AND id != :addressId")
    Mono<Void> clearDefaultForUser(UUID userId, UUID addressId);

    Mono<Long> countByUserIdAndIsActiveTrue(UUID userId);
}
