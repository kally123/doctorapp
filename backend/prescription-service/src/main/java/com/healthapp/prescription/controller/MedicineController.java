package com.healthapp.prescription.controller;

import com.healthapp.prescription.dto.MedicineSearchResult;
import com.healthapp.prescription.service.MedicineSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for medicine search.
 * Disabled in test profile to avoid requiring Elasticsearch during tests.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/medicines")
@Profile("!test")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineSearchService medicineSearchService;

    /**
     * Search medicines with autocomplete.
     */
    @GetMapping("/search")
    public Flux<MedicineSearchResult> searchMedicines(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("Searching medicines with query: {}, limit: {}", query, limit);
        return medicineSearchService.searchMedicines(query, limit);
    }

    /**
     * Get medicine by ID.
     */
    @GetMapping("/{medicineId}")
    public Mono<ResponseEntity<MedicineSearchResult>> getMedicine(@PathVariable String medicineId) {
        return medicineSearchService.getMedicineById(medicineId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Find generic alternatives.
     */
    @GetMapping("/alternatives")
    public Flux<MedicineSearchResult> findAlternatives(@RequestParam String genericName) {
        return medicineSearchService.findAlternatives(genericName);
    }

    /**
     * Get medicines by category.
     */
    @GetMapping("/category/{category}")
    public Flux<MedicineSearchResult> getMedicinesByCategory(@PathVariable String category) {
        return medicineSearchService.getMedicinesByCategory(category);
    }
}
