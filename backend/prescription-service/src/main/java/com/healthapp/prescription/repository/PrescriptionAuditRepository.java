package com.healthapp.prescription.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.prescription.domain.PrescriptionAudit;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import org.springframework.context.annotation.Profile;
import java.util.UUID;

@Profile("!test")
@Repository
public interface PrescriptionAuditRepository extends R2dbcRepository<PrescriptionAudit, UUID> {

    Flux<PrescriptionAudit> findByPrescriptionIdOrderByCreatedAtDesc(UUID prescriptionId);
    
    Flux<PrescriptionAudit> findByActorIdOrderByCreatedAtDesc(UUID actorId);
}
