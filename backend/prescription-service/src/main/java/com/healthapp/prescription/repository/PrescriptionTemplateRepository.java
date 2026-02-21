package com.healthapp.prescription.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.prescription.domain.PrescriptionTemplate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Profile;
import java.util.UUID;

@Profile("!test")
@Repository
public interface PrescriptionTemplateRepository extends R2dbcRepository<PrescriptionTemplate, UUID> {

    Flux<PrescriptionTemplate> findByDoctorIdAndIsActiveOrderByUsageCountDesc(UUID doctorId, Boolean isActive);
    
    Mono<PrescriptionTemplate> findByDoctorIdAndTemplateName(UUID doctorId, String templateName);
    
    Flux<PrescriptionTemplate> findByDiagnosisContainingIgnoreCaseAndIsActive(String diagnosis, Boolean isActive);
    
    @Query("SELECT * FROM prescription_templates WHERE (doctor_id = :doctorId OR is_public = true) AND is_active = true ORDER BY usage_count DESC")
    Flux<PrescriptionTemplate> findAvailableTemplates(UUID doctorId);
    
    @Query("UPDATE prescription_templates SET usage_count = usage_count + 1, last_used_at = NOW() WHERE id = :templateId")
    Mono<Void> incrementUsageCount(UUID templateId);
}
