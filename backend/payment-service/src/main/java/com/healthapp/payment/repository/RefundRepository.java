package com.healthapp.payment.repository;

import com.healthapp.payment.domain.Refund;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface RefundRepository extends ReactiveCrudRepository<Refund, UUID> {
    
    Flux<Refund> findByPaymentId(UUID paymentId);
}
