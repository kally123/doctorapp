package com.healthapp.order.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.domain.PartnerInventory;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository for PartnerInventory entity.
 */
@Profile("!test")
@Repository
public interface PartnerInventoryRepository extends R2dbcRepository<PartnerInventory, UUID> {

    Flux<PartnerInventory> findByPartnerId(UUID partnerId);

    Mono<PartnerInventory> findByPartnerIdAndProductId(UUID partnerId, String productId);

    @Query("SELECT * FROM partner_inventory WHERE partner_id = :partnerId AND is_available = true AND quantity_available > 0")
    Flux<PartnerInventory> findAvailableByPartnerId(UUID partnerId);

    @Query("DELETE FROM partner_inventory WHERE partner_id = :partnerId")
    Mono<Void> deleteByPartnerId(UUID partnerId);
}
