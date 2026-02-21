package com.healthapp.order.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.domain.Phlebotomist;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Repository for Phlebotomist entity.
 */
@Profile("!test")
@Repository
public interface PhlebotomistRepository extends R2dbcRepository<Phlebotomist, UUID> {

    Flux<Phlebotomist> findByLabPartnerIdAndIsActiveTrue(UUID labPartnerId);
}
