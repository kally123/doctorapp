package com.healthapp.order.controller;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.dto.LabTestResponse;
import com.healthapp.order.dto.TestPackageResponse;
import com.healthapp.order.service.LabBookingService;
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
 * REST controller for lab test catalog browsing.
 */
@Slf4j
@Profile("!test")
@RestController
@RequestMapping("/api/v1/lab-tests")
@RequiredArgsConstructor
@Tag(name = "Lab Tests", description = "Lab test catalog browsing APIs")
public class LabTestController {

    private final LabBookingService labBookingService;

    @GetMapping("/search")
    @Operation(summary = "Search lab tests", description = "Search lab tests by keyword")
    public Flux<LabTestResponse> searchLabTests(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Searching lab tests with keyword: {}", keyword);
        return labBookingService.searchLabTests(keyword, page, size);
    }

    @GetMapping("/{testId}")
    @Operation(summary = "Get lab test", description = "Get lab test details by ID")
    public Mono<ResponseEntity<LabTestResponse>> getLabTest(
            @PathVariable UUID testId) {
        log.info("Getting lab test: {}", testId);
        return labBookingService.getLabTest(testId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get tests by category", description = "Get all lab tests in a category")
    public Flux<LabTestResponse> getTestsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting lab tests for category: {}", categoryId);
        return labBookingService.getTestsByCategory(categoryId, page, size);
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular tests", description = "Get popular/frequently booked lab tests")
    public Flux<LabTestResponse> getPopularTests(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Getting popular lab tests, limit: {}", limit);
        return labBookingService.getPopularTests(limit);
    }

    @GetMapping("/packages")
    @Operation(summary = "Get test packages", description = "Get all available test packages")
    public Flux<TestPackageResponse> getTestPackages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting test packages");
        return labBookingService.getTestPackages(page, size);
    }

    @GetMapping("/packages/{packageId}")
    @Operation(summary = "Get test package", description = "Get test package details by ID")
    public Mono<ResponseEntity<TestPackageResponse>> getTestPackage(
            @PathVariable UUID packageId) {
        log.info("Getting test package: {}", packageId);
        return labBookingService.getTestPackage(packageId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/packages/popular")
    @Operation(summary = "Get popular packages", description = "Get popular/frequently booked test packages")
    public Flux<TestPackageResponse> getPopularPackages(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Getting popular test packages, limit: {}", limit);
        return labBookingService.getPopularPackages(limit);
    }
}
