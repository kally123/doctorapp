package com.healthapp.search.controller;

import com.healthapp.common.dto.ApiResponse;
import com.healthapp.search.model.dto.*;
import com.healthapp.search.service.DoctorSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller for doctor search operations.
 * Disabled in test profile to avoid requiring Elasticsearch during tests.
 */
@Slf4j
@RestController
@Profile("!test")
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final DoctorSearchService searchService;
    
    /**
     * Search for doctors with filters and pagination.
     */
    @PostMapping("/doctors")
    public Mono<ApiResponse<DoctorSearchResponse>> searchDoctors(
            @Valid @RequestBody DoctorSearchRequest request) {
        log.debug("Doctor search request: query={}, city={}, specialization={}",
                request.getQuery(), request.getCity(), request.getSpecialization());
        
        return searchService.search(request)
                .map(ApiResponse::success);
    }
    
    /**
     * Simple GET search endpoint for basic queries.
     */
    @GetMapping("/doctors")
    public Mono<ApiResponse<DoctorSearchResponse>> searchDoctorsGet(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean verifiedOnly,
            @RequestParam(defaultValue = "RELEVANCE") DoctorSearchRequest.SortField sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        DoctorSearchRequest request = DoctorSearchRequest.builder()
                .query(query)
                .specialization(specialization)
                .city(city)
                .minRating(minRating)
                .verifiedOnly(verifiedOnly)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();
        
        return searchService.search(request)
                .map(ApiResponse::success);
    }
    
    /**
     * Autocomplete suggestions for doctor search.
     */
    @GetMapping("/doctors/autocomplete")
    public Mono<ApiResponse<List<String>>> autocomplete(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        
        return searchService.autocomplete(query, Math.min(limit, 10))
                .map(ApiResponse::success);
    }
    
    /**
     * Search doctors near a location.
     */
    @GetMapping("/doctors/nearby")
    public Mono<ApiResponse<DoctorSearchResponse>> searchNearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") int radiusKm,
            @RequestParam(required = false) String specialization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        DoctorSearchRequest request = DoctorSearchRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .radiusKm(radiusKm)
                .specialization(specialization)
                .sortBy(DoctorSearchRequest.SortField.DISTANCE)
                .sortDirection(DoctorSearchRequest.SortDirection.ASC)
                .page(page)
                .size(size)
                .build();
        
        return searchService.search(request)
                .map(ApiResponse::success);
    }
}
