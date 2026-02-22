package com.healthapp.order.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.domain.OrderItem;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository for OrderItem entity.
 */
@Profile("!test")
@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItem, UUID> {

    Flux<OrderItem> findByOrderId(UUID orderId);

    Mono<Void> deleteByOrderId(UUID orderId);

    Flux<OrderItem> findByPrescriptionItemId(UUID prescriptionItemId);
}
