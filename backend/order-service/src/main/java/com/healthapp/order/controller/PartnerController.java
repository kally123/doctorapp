package com.healthapp.order.controller;

import com.healthapp.order.domain.Partner;
import com.healthapp.order.domain.enums.PartnerType;
import com.healthapp.order.repository.PartnerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for partner (pharmacy/lab) browsing.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
@Tag(name = "Partners", description = "Pharmacy and lab partner browsing APIs")
public class PartnerController {

    private final PartnerRepository partnerRepository;

    @GetMapping
    @Operation(summary = "Get all partners", description = "Get all active partners by type")
    public Flux<Partner> getPartners(
            @RequestParam PartnerType type,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting partners of type: {}, city: {}", type, city);
        if (city != null && !city.isEmpty()) {
            return partnerRepository.findByTypeAndCityAndIsActiveTrue(type, city)
                    .skip((long) page * size)
                    .take(size);
        }
        return partnerRepository.findByTypeAndIsActiveTrue(type)
                .skip((long) page * size)
                .take(size);
    }

    @GetMapping("/{partnerId}")
    @Operation(summary = "Get partner", description = "Get partner details by ID")
    public Mono<ResponseEntity<Partner>> getPartner(
            @PathVariable UUID partnerId) {
        log.info("Getting partner: {}", partnerId);
        return partnerRepository.findById(partnerId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/nearby")
    @Operation(summary = "Get nearby partners", description = "Get partners near a location")
    public Flux<Partner> getNearbyPartners(
            @RequestParam PartnerType type,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5") double radiusKm,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Getting nearby partners of type: {} at ({}, {}), radius: {}km", 
                type, latitude, longitude, radiusKm);
        return partnerRepository.findNearbyPartners(type, latitude, longitude, radiusKm, limit);
    }

    @GetMapping("/pharmacies")
    @Operation(summary = "Get pharmacies", description = "Get all active pharmacies")
    public Flux<Partner> getPharmacies(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting pharmacies, city: {}", city);
        if (city != null && !city.isEmpty()) {
            return partnerRepository.findByTypeAndCityAndIsActiveTrue(PartnerType.PHARMACY, city)
                    .skip((long) page * size)
                    .take(size);
        }
        return partnerRepository.findByTypeAndIsActiveTrue(PartnerType.PHARMACY)
                .skip((long) page * size)
                .take(size);
    }

    @GetMapping("/labs")
    @Operation(summary = "Get labs", description = "Get all active lab partners")
    public Flux<Partner> getLabs(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting labs, city: {}", city);
        if (city != null && !city.isEmpty()) {
            return partnerRepository.findByTypeAndCityAndIsActiveTrue(PartnerType.LAB, city)
                    .skip((long) page * size)
                    .take(size);
        }
        return partnerRepository.findByTypeAndIsActiveTrue(PartnerType.LAB)
                .skip((long) page * size)
                .take(size);
    }
}
