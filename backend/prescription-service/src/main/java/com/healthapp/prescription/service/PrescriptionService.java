package com.healthapp.prescription.service;

import org.springframework.context.annotation.Profile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthapp.prescription.domain.Prescription;
import com.healthapp.prescription.domain.PrescriptionAudit;
import com.healthapp.prescription.domain.PrescriptionItem;
import com.healthapp.prescription.domain.enums.AuditAction;
import com.healthapp.prescription.domain.enums.PrescriptionStatus;
import com.healthapp.prescription.dto.*;
import com.healthapp.prescription.event.PrescriptionEventPublisher;
import com.healthapp.prescription.repository.PrescriptionAuditRepository;
import com.healthapp.prescription.repository.PrescriptionItemRepository;
import com.healthapp.prescription.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Profile;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main service for prescription management.
 */
@Slf4j
@Profile("!test")
@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository itemRepository;
    private final PrescriptionAuditRepository auditRepository;
    private final DigitalSignatureService signatureService;
    private final PdfGenerationService pdfService;
    private final PrescriptionEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * Create a new prescription (draft).
     */
    @Transactional
    public Mono<PrescriptionResponse> createPrescription(CreatePrescriptionRequest request) {
        return generatePrescriptionNumber(request.getDoctorId())
                .flatMap(prescriptionNumber -> {
                    Prescription prescription = Prescription.builder()
                            .consultationId(request.getConsultationId())
                            .appointmentId(request.getAppointmentId())
                            .patientId(request.getPatientId())
                            .doctorId(request.getDoctorId())
                            .prescriptionNumber(prescriptionNumber)
                            .prescriptionDate(request.getPrescriptionDate() != null 
                                    ? request.getPrescriptionDate() : LocalDate.now())
                            .validUntil(request.getValidUntil() != null 
                                    ? request.getValidUntil() : LocalDate.now().plusMonths(3))
                            .diagnosis(request.getDiagnosis())
                            .chiefComplaints(request.getChiefComplaints())
                            .clinicalNotes(request.getClinicalNotes())
                            .generalAdvice(request.getGeneralAdvice())
                            .dietAdvice(request.getDietAdvice())
                            .followUpDate(request.getFollowUpDate())
                            .followUpNotes(request.getFollowUpNotes())
                            .labTestsRecommended(serializeLabTests(request.getLabTestsRecommended()))
                            .status(PrescriptionStatus.DRAFT.name())
                            .templateId(request.getTemplateId())
                            .createdBy(request.getDoctorId())
                            .build();

                    return prescriptionRepository.save(prescription);
                })
                .flatMap(prescription -> saveItems(prescription, request.getItems())
                        .collectList()
                        .flatMap(items -> {
                            // Create audit entry
                            createAuditEntry(prescription.getId(), AuditAction.CREATED, 
                                    request.getDoctorId(), null, PrescriptionStatus.DRAFT.name())
                                    .subscribe();
                            
                            // Publish event
                            eventPublisher.publishPrescriptionCreated(prescription, items);
                            
                            return toResponse(prescription, items);
                        }));
    }

    /**
     * Get prescription by ID.
     */
    public Mono<PrescriptionResponse> getPrescription(UUID prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .flatMap(prescription -> 
                        itemRepository.findByPrescriptionIdOrderBySequenceOrder(prescriptionId)
                                .collectList()
                                .flatMap(items -> toResponse(prescription, items)));
    }

    /**
     * Get prescription by prescription number.
     */
    public Mono<PrescriptionResponse> getPrescriptionByNumber(String prescriptionNumber) {
        return prescriptionRepository.findByPrescriptionNumber(prescriptionNumber)
                .flatMap(prescription -> 
                        itemRepository.findByPrescriptionIdOrderBySequenceOrder(prescription.getId())
                                .collectList()
                                .flatMap(items -> toResponse(prescription, items)));
    }

    /**
     * Get all prescriptions for a patient.
     */
    public Flux<PrescriptionResponse> getPatientPrescriptions(UUID patientId) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .flatMap(prescription -> 
                        itemRepository.findByPrescriptionIdOrderBySequenceOrder(prescription.getId())
                                .collectList()
                                .flatMap(items -> toResponse(prescription, items)));
    }

    /**
     * Get all prescriptions by a doctor.
     */
    public Flux<PrescriptionResponse> getDoctorPrescriptions(UUID doctorId) {
        return prescriptionRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId)
                .flatMap(prescription -> 
                        itemRepository.findByPrescriptionIdOrderBySequenceOrder(prescription.getId())
                                .collectList()
                                .flatMap(items -> toResponse(prescription, items)));
    }

    /**
     * Sign a prescription.
     */
    @Transactional
    public Mono<PrescriptionResponse> signPrescription(UUID prescriptionId, UUID doctorId) {
        return prescriptionRepository.findById(prescriptionId)
                .filter(p -> p.getDoctorId().equals(doctorId))
                .filter(p -> PrescriptionStatus.DRAFT.name().equals(p.getStatus()))
                .flatMap(prescription -> 
                        itemRepository.findByPrescriptionIdOrderBySequenceOrder(prescriptionId)
                                .collectList()
                                .flatMap(items -> signatureService.signPrescription(prescription, items)
                                        .flatMap(signatureResult -> {
                                            prescription.setStatus(PrescriptionStatus.SIGNED.name());
                                            prescription.setSignedAt(signatureResult.getSignedAt());
                                            prescription.setSignatureHash(signatureResult.getSignatureHash());
                                            prescription.setCertificateSerial(signatureResult.getCertificateSerial());
                                            
                                            return prescriptionRepository.save(prescription)
                                                    .flatMap(signed -> {
                                                        // Generate PDF
                                                        return pdfService.generateAndStorePdf(
                                                                signed, items, null, null)
                                                                .flatMap(pdfResult -> {
                                                                    signed.setPdfUrl(pdfResult.getPresignedUrl());
                                                                    signed.setPdfS3Key(pdfResult.getS3Key());
                                                                    signed.setPdfGeneratedAt(Instant.now());
                                                                    return prescriptionRepository.save(signed);
                                                                })
                                                                .onErrorResume(e -> {
                                                                    log.error("PDF generation failed for prescription: {}", 
                                                                            signed.getPrescriptionNumber(), e);
                                                                    return Mono.just(signed);
                                                                });
                                                    })
                                                    .flatMap(finalPrescription -> {
                                                        // Create audit entry
                                                        createAuditEntry(prescriptionId, AuditAction.SIGNED, 
                                                                doctorId, PrescriptionStatus.DRAFT.name(), 
                                                                PrescriptionStatus.SIGNED.name()).subscribe();
                                                        
                                                        // Publish event
                                                        eventPublisher.publishPrescriptionSigned(finalPrescription, items);
                                                        
                                                        return toResponse(finalPrescription, items);
                                                    });
                                        })));
    }

    /**
     * Cancel a prescription.
     */
    @Transactional
    public Mono<PrescriptionResponse> cancelPrescription(UUID prescriptionId, UUID doctorId, String reason) {
        return prescriptionRepository.findById(prescriptionId)
                .filter(p -> p.getDoctorId().equals(doctorId))
                .filter(p -> !PrescriptionStatus.DISPENSED.name().equals(p.getStatus()))
                .flatMap(prescription -> {
                    String previousStatus = prescription.getStatus();
                    prescription.setStatus(PrescriptionStatus.CANCELLED.name());
                    
                    return prescriptionRepository.save(prescription)
                            .flatMap(cancelled -> 
                                    itemRepository.findByPrescriptionIdOrderBySequenceOrder(prescriptionId)
                                            .collectList()
                                            .flatMap(items -> {
                                                // Create audit entry
                                                createAuditEntry(prescriptionId, AuditAction.CANCELLED, 
                                                        doctorId, previousStatus, PrescriptionStatus.CANCELLED.name())
                                                        .subscribe();
                                                
                                                // Publish event
                                                eventPublisher.publishPrescriptionCancelled(cancelled);
                                                
                                                return toResponse(cancelled, items);
                                            }));
                });
    }

    /**
     * Get prescription for consultation.
     */
    public Mono<PrescriptionResponse> getPrescriptionByConsultation(UUID consultationId) {
        return prescriptionRepository.findByConsultationId(consultationId)
                .flatMap(prescription -> 
                        itemRepository.findByPrescriptionIdOrderBySequenceOrder(prescription.getId())
                                .collectList()
                                .flatMap(items -> toResponse(prescription, items)));
    }

    /**
     * Verify prescription signature.
     */
    public Mono<Boolean> verifyPrescription(UUID prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .filter(p -> p.getSignatureHash() != null)
                .flatMap(prescription -> 
                        itemRepository.findByPrescriptionIdOrderBySequenceOrder(prescriptionId)
                                .collectList()
                                .flatMap(items -> signatureService.verifySignature(
                                        prescription, items, prescription.getSignatureHash())));
    }

    private Mono<String> generatePrescriptionNumber(UUID doctorId) {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return prescriptionRepository.countByDoctorIdAndPrescriptionDate(doctorId, LocalDate.now())
                .map(count -> String.format("RX-%s-%04d", datePrefix, count + 1));
    }

    private Flux<PrescriptionItem> saveItems(Prescription prescription, List<PrescriptionItemRequest> itemRequests) {
        AtomicInteger sequence = new AtomicInteger(0);
        
        return Flux.fromIterable(itemRequests)
                .map(req -> PrescriptionItem.builder()
                        .prescriptionId(prescription.getId())
                        .medicineId(req.getMedicineId())
                        .medicineName(req.getMedicineName())
                        .genericName(req.getGenericName())
                        .manufacturer(req.getManufacturer())
                        .strength(req.getStrength())
                        .formulation(req.getFormulation())
                        .dosage(req.getDosage())
                        .frequency(req.getFrequency())
                        .duration(req.getDuration())
                        .timing(req.getTiming())
                        .route(req.getRoute())
                        .quantity(req.getQuantity())
                        .quantityUnit(req.getQuantityUnit())
                        .specialInstructions(req.getSpecialInstructions())
                        .sequenceOrder(req.getSequenceOrder() != null 
                                ? req.getSequenceOrder() : sequence.incrementAndGet())
                        .build())
                .flatMap(itemRepository::save);
    }

    private Mono<PrescriptionAudit> createAuditEntry(UUID prescriptionId, AuditAction action, 
                                                      UUID actorId, String previousStatus, String newStatus) {
        PrescriptionAudit audit = PrescriptionAudit.builder()
                .prescriptionId(prescriptionId)
                .action(action.name())
                .actorId(actorId)
                .actorType("DOCTOR")
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .build();
        
        return auditRepository.save(audit);
    }

    private String serializeLabTests(List<String> labTests) {
        if (labTests == null || labTests.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(labTests);
        } catch (JsonProcessingException e) {
            return String.join(", ", labTests);
        }
    }

    private List<String> deserializeLabTests(String labTests) {
        if (labTests == null || labTests.isEmpty()) return List.of();
        try {
            return objectMapper.readValue(labTests, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of(labTests.split(",\\s*"));
        }
    }

    private Mono<PrescriptionResponse> toResponse(Prescription prescription, List<PrescriptionItem> items) {
        List<PrescriptionItemResponse> itemResponses = items.stream()
                .map(item -> PrescriptionItemResponse.builder()
                        .id(item.getId())
                        .medicineId(item.getMedicineId())
                        .medicineName(item.getMedicineName())
                        .genericName(item.getGenericName())
                        .manufacturer(item.getManufacturer())
                        .strength(item.getStrength())
                        .formulation(item.getFormulation())
                        .dosage(item.getDosage())
                        .frequency(item.getFrequency())
                        .duration(item.getDuration())
                        .timing(item.getTiming())
                        .route(item.getRoute())
                        .quantity(item.getQuantity())
                        .quantityUnit(item.getQuantityUnit())
                        .specialInstructions(item.getSpecialInstructions())
                        .sequenceOrder(item.getSequenceOrder())
                        .isDispensed(item.getIsDispensed())
                        .dispensedQuantity(item.getDispensedQuantity())
                        .build())
                .toList();

        return Mono.just(PrescriptionResponse.builder()
                .id(prescription.getId())
                .prescriptionNumber(prescription.getPrescriptionNumber())
                .consultationId(prescription.getConsultationId())
                .appointmentId(prescription.getAppointmentId())
                .patientId(prescription.getPatientId())
                .doctorId(prescription.getDoctorId())
                .prescriptionDate(prescription.getPrescriptionDate())
                .validUntil(prescription.getValidUntil())
                .diagnosis(prescription.getDiagnosis())
                .chiefComplaints(prescription.getChiefComplaints())
                .clinicalNotes(prescription.getClinicalNotes())
                .generalAdvice(prescription.getGeneralAdvice())
                .dietAdvice(prescription.getDietAdvice())
                .followUpDate(prescription.getFollowUpDate())
                .followUpNotes(prescription.getFollowUpNotes())
                .labTestsRecommended(deserializeLabTests(prescription.getLabTestsRecommended()))
                .status(prescription.getStatus())
                .signedAt(prescription.getSignedAt())
                .isSigned(prescription.getSignedAt() != null)
                .pdfUrl(prescription.getPdfUrl())
                .items(itemResponses)
                .createdAt(prescription.getCreatedAt())
                .updatedAt(prescription.getUpdatedAt())
                .build());
    }
}
