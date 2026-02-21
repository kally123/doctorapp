package com.healthapp.prescription.controller;

import org.springframework.context.annotation.Profile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthapp.prescription.domain.PrescriptionTemplate;
import com.healthapp.prescription.dto.CreateTemplateRequest;
import com.healthapp.prescription.dto.PrescriptionItemRequest;
import com.healthapp.prescription.dto.TemplateResponse;
import com.healthapp.prescription.repository.PrescriptionTemplateRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Profile;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for prescription templates.
 */
@Slf4j
@Profile("!test")
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final PrescriptionTemplateRepository templateRepository;
    private final ObjectMapper objectMapper;

    /**
     * Create a new template.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TemplateResponse> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        log.info("Creating template: {} for doctor: {}", request.getTemplateName(), request.getDoctorId());
        
        PrescriptionTemplate template = PrescriptionTemplate.builder()
                .doctorId(request.getDoctorId())
                .templateName(request.getTemplateName())
                .description(request.getDescription())
                .diagnosis(request.getDiagnosis())
                .specialization(request.getSpecialization())
                .templateItems(serializeItems(request.getItems()))
                .generalAdvice(request.getGeneralAdvice())
                .dietAdvice(request.getDietAdvice())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                .build();
        
        return templateRepository.save(template)
                .map(this::toResponse);
    }

    /**
     * Get template by ID.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<TemplateResponse>> getTemplate(@PathVariable UUID id) {
        return templateRepository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get templates for a doctor.
     */
    @GetMapping("/doctor/{doctorId}")
    public Flux<TemplateResponse> getDoctorTemplates(@PathVariable UUID doctorId) {
        return templateRepository.findByDoctorIdAndIsActiveOrderByUsageCountDesc(doctorId, true)
                .map(this::toResponse);
    }

    /**
     * Get available templates (own + public).
     */
    @GetMapping("/available")
    public Flux<TemplateResponse> getAvailableTemplates(
            @RequestHeader("X-Doctor-Id") UUID doctorId) {
        return templateRepository.findAvailableTemplates(doctorId)
                .map(this::toResponse);
    }

    /**
     * Search templates by diagnosis.
     */
    @GetMapping("/search")
    public Flux<TemplateResponse> searchTemplates(@RequestParam String diagnosis) {
        return templateRepository.findByDiagnosisContainingIgnoreCaseAndIsActive(diagnosis, true)
                .map(this::toResponse);
    }

    /**
     * Delete (deactivate) a template.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteTemplate(
            @PathVariable UUID id,
            @RequestHeader("X-Doctor-Id") UUID doctorId) {
        return templateRepository.findById(id)
                .filter(t -> t.getDoctorId().equals(doctorId))
                .flatMap(template -> {
                    template.setIsActive(false);
                    return templateRepository.save(template);
                })
                .map(t -> ResponseEntity.noContent().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Use a template (increment usage count).
     */
    @PostMapping("/{id}/use")
    public Mono<ResponseEntity<Void>> useTemplate(@PathVariable UUID id) {
        return templateRepository.incrementUsageCount(id)
                .thenReturn(ResponseEntity.ok().<Void>build())
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    private String serializeItems(List<PrescriptionItemRequest> items) {
        if (items == null) return "[]";
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<PrescriptionItemRequest> deserializeItems(String items) {
        if (items == null || items.isEmpty()) return List.of();
        try {
            return objectMapper.readValue(items, new TypeReference<List<PrescriptionItemRequest>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private TemplateResponse toResponse(PrescriptionTemplate template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .doctorId(template.getDoctorId())
                .templateName(template.getTemplateName())
                .description(template.getDescription())
                .diagnosis(template.getDiagnosis())
                .specialization(template.getSpecialization())
                .items(deserializeItems(template.getTemplateItems()))
                .generalAdvice(template.getGeneralAdvice())
                .dietAdvice(template.getDietAdvice())
                .usageCount(template.getUsageCount())
                .lastUsedAt(template.getLastUsedAt())
                .isActive(template.getIsActive())
                .isPublic(template.getIsPublic())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
