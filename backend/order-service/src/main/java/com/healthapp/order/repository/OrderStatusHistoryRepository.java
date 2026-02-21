package com.healthapp.order.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.domain.OrderStatusHistory;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Repository for OrderStatusHistory entity.
 */
@Profile("!test")
@Repository
public interface OrderStatusHistoryRepository extends R2dbcRepository<OrderStatusHistory, UUID> {

    Flux<OrderStatusHistory> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
