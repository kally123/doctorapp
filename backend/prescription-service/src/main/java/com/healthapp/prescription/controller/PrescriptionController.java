package com.healthapp.prescription.controller;

import org.springframework.context.annotation.Profile;
import com.healthapp.prescription.dto.CreatePrescriptionRequest;
import com.healthapp.prescription.dto.PrescriptionResponse;
import com.healthapp.prescription.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Profile;
import java.util.UUID;

/**
 * REST controller for prescription management.
 */
@Slf4j
@Profile("!test")
@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    /**
     * Create a new prescription (draft).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PrescriptionResponse> createPrescription(
            @Valid @RequestBody CreatePrescriptionRequest request) {
        log.info("Creating prescription for patient: {} by doctor: {}", 
                request.getPatientId(), request.getDoctorId());
        return prescriptionService.createPrescription(request);
    }

    /**
     * Get prescription by ID.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<PrescriptionResponse>> getPrescription(@PathVariable UUID id) {
        return prescriptionService.getPrescription(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get prescription by prescription number.
     */
    @GetMapping("/number/{prescriptionNumber}")
    public Mono<ResponseEntity<PrescriptionResponse>> getPrescriptionByNumber(
            @PathVariable String prescriptionNumber) {
        return prescriptionService.getPrescriptionByNumber(prescriptionNumber)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get prescriptions for a patient.
     */
    @GetMapping("/patient/{patientId}")
    public Flux<PrescriptionResponse> getPatientPrescriptions(@PathVariable UUID patientId) {
        return prescriptionService.getPatientPrescriptions(patientId);
    }

    /**
     * Get prescriptions by a doctor.
     */
    @GetMapping("/doctor/{doctorId}")
    public Flux<PrescriptionResponse> getDoctorPrescriptions(@PathVariable UUID doctorId) {
        return prescriptionService.getDoctorPrescriptions(doctorId);
    }

    /**
     * Get prescription for a consultation.
     */
    @GetMapping("/consultation/{consultationId}")
    public Mono<ResponseEntity<PrescriptionResponse>> getPrescriptionByConsultation(
            @PathVariable UUID consultationId) {
        return prescriptionService.getPrescriptionByConsultation(consultationId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Sign a prescription.
     */
    @PostMapping("/{id}/sign")
    public Mono<ResponseEntity<PrescriptionResponse>> signPrescription(
            @PathVariable UUID id,
            @RequestHeader("X-Doctor-Id") UUID doctorId) {
        log.info("Signing prescription: {} by doctor: {}", id, doctorId);
        return prescriptionService.signPrescription(id, doctorId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Cancel a prescription.
     */
    @PostMapping("/{id}/cancel")
    public Mono<ResponseEntity<PrescriptionResponse>> cancelPrescription(
            @PathVariable UUID id,
            @RequestHeader("X-Doctor-Id") UUID doctorId,
            @RequestParam(required = false) String reason) {
        log.info("Cancelling prescription: {} by doctor: {}", id, doctorId);
        return prescriptionService.cancelPrescription(id, doctorId, reason)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Verify prescription signature.
     */
    @GetMapping("/{id}/verify")
    public Mono<ResponseEntity<VerificationResponse>> verifyPrescription(@PathVariable UUID id) {
        return prescriptionService.verifyPrescription(id)
                .map(isValid -> ResponseEntity.ok(new VerificationResponse(id, isValid)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    record VerificationResponse(UUID prescriptionId, boolean isValid) {}
}
