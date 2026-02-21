package com.healthapp.prescription.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.prescription.domain.PrescriptionItem;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Profile;
import java.util.UUID;

@Profile("!test")
@Repository
public interface PrescriptionItemRepository extends R2dbcRepository<PrescriptionItem, UUID> {

    Flux<PrescriptionItem> findByPrescriptionIdOrderBySequenceOrder(UUID prescriptionId);
    
    Mono<Void> deleteByPrescriptionId(UUID prescriptionId);
    
    Flux<PrescriptionItem> findByPrescriptionIdAndIsDispensed(UUID prescriptionId, Boolean isDispensed);
}
