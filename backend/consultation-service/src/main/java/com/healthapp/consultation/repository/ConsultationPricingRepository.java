package com.healthapp.consultation.repository;

import com.healthapp.consultation.domain.ConsultationPricing;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ConsultationPricingRepository extends ReactiveCrudRepository<ConsultationPricing, UUID> {
    
    Flux<ConsultationPricing> findByDoctorId(UUID doctorId);
    
    Mono<ConsultationPricing> findByDoctorIdAndConsultationMode(UUID doctorId, String consultationMode);
    
    Flux<ConsultationPricing> findByDoctorIdAndIsActive(UUID doctorId, Boolean isActive);
}
