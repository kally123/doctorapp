package com.healthapp.payment.repository;

import com.healthapp.payment.domain.PaymentTransaction;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface PaymentRepository extends ReactiveCrudRepository<PaymentTransaction, UUID> {
    
    Mono<PaymentTransaction> findByGatewayOrderId(String gatewayOrderId);
    
    Mono<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);
    
    Flux<PaymentTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    @Query("""
        SELECT * FROM payment_transactions 
        WHERE user_id = :userId 
        ORDER BY created_at DESC 
        LIMIT :size OFFSET :offset
        """)
    Flux<PaymentTransaction> findByUserIdPaginated(UUID userId, int size, int offset);
    
    @Query("""
        SELECT * FROM payment_transactions 
        WHERE order_type = :orderType 
        AND order_id = :orderId
        """)
    Mono<PaymentTransaction> findByOrderTypeAndOrderId(String orderType, UUID orderId);
}
