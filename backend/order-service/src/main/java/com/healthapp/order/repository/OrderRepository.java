package com.healthapp.order.repository;

import com.healthapp.order.domain.Order;
import com.healthapp.order.domain.enums.OrderStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository for Order entity.
 */
@Repository
public interface OrderRepository extends R2dbcRepository<Order, UUID> {

    Mono<Order> findByOrderNumber(String orderNumber);

    Flux<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Flux<Order> findByPartnerIdOrderByCreatedAtDesc(UUID partnerId);

    Flux<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);

    Flux<Order> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, OrderStatus status);

    Flux<Order> findByPartnerIdAndStatus(UUID partnerId, OrderStatus status);

    Flux<Order> findByPartnerIdAndStatusOrderByCreatedAtDesc(UUID partnerId, OrderStatus status);

    @Query("SELECT * FROM orders WHERE user_id = :userId AND status NOT IN ('CART', 'CANCELLED', 'REFUNDED') ORDER BY created_at DESC LIMIT :limit")
    Flux<Order> findRecentOrdersByUserId(UUID userId, int limit);

    @Query("SELECT * FROM orders WHERE partner_id = :partnerId AND status IN ('CONFIRMED', 'PROCESSING', 'PACKED') ORDER BY created_at ASC")
    Flux<Order> findPendingOrdersForPartner(UUID partnerId);

    Mono<Long> countByUserId(UUID userId);

    Mono<Long> countByPartnerIdAndStatus(UUID partnerId, OrderStatus status);
}
