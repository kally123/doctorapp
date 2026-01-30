package com.healthapp.doctor.controller;

import com.healthapp.common.dto.ApiResponse;
import com.healthapp.common.dto.PageRequest;
import com.healthapp.common.dto.PageResponse;
import com.healthapp.doctor.model.dto.*;
import com.healthapp.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for doctor profile management.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {
    
    private final DoctorService doctorService;
    
    /**
     * Creates a new doctor profile.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiResponse<DoctorDto>> createDoctor(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateDoctorRequest request) {
        log.info("Creating doctor profile for user: {}", userId);
        
        return doctorService.createDoctor(userId, request)
                .map(doctor -> ApiResponse.success(doctor, "Doctor profile created successfully"));
    }
    
    /**
     * Gets the current user's doctor profile.
     */
    @GetMapping("/me")
    public Mono<ApiResponse<DoctorDto>> getMyProfile(
            @RequestHeader("X-User-Id") UUID userId) {
        
        return doctorService.getDoctorByUserId(userId)
                .map(ApiResponse::success);
    }
    
    /**
     * Updates the current user's doctor profile.
     */
    @PutMapping("/me")
    public Mono<ApiResponse<DoctorDto>> updateMyProfile(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UpdateDoctorRequest request) {
        
        return doctorService.getDoctorByUserId(userId)
                .flatMap(doctor -> doctorService.updateDoctor(UUID.fromString(doctor.getId()), request))
                .map(doctor -> ApiResponse.success(doctor, "Profile updated successfully"));
    }
    
    /**
     * Gets a doctor by ID (public endpoint).
     */
    @GetMapping("/{id}")
    public Mono<ApiResponse<DoctorDto>> getDoctorById(@PathVariable UUID id) {
        return doctorService.getDoctorById(id)
                .map(ApiResponse::success);
    }
    
    /**
     * Gets top rated doctors (public endpoint).
     */
    @GetMapping("/top")
    public Mono<ApiResponse<List<DoctorDto>>> getTopDoctors(
            @RequestParam(defaultValue = "10") int limit) {
        
        return doctorService.getTopDoctors(Math.min(limit, 50))
                .collectList()
                .map(ApiResponse::success);
    }
    
    /**
     * Gets doctors by city (public endpoint).
     */
    @GetMapping("/city/{city}")
    public Mono<ApiResponse<PageResponse<DoctorDto>>> getDoctorsByCity(
            @PathVariable String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(Math.min(size, 100))
                .build();
        
        return doctorService.getDoctorsByCity(city, pageRequest)
                .map(ApiResponse::success);
    }
    
    /**
     * Toggles accepting patients status.
     */
    @PostMapping("/me/toggle-availability")
    public Mono<ApiResponse<DoctorDto>> toggleAvailability(
            @RequestHeader("X-User-Id") UUID userId) {
        
        return doctorService.getDoctorByUserId(userId)
                .flatMap(doctor -> doctorService.toggleAcceptingPatients(UUID.fromString(doctor.getId())))
                .map(doctor -> ApiResponse.success(doctor, "Availability updated"));
    }
    
    /**
     * Verifies a doctor (admin only).
     */
    @PostMapping("/{id}/verify")
    public Mono<ApiResponse<DoctorDto>> verifyDoctor(@PathVariable UUID id) {
        return doctorService.verifyDoctor(id)
                .map(doctor -> ApiResponse.success(doctor, "Doctor verified successfully"));
    }
}
