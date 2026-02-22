package com.healthapp.prescription.service;

import com.healthapp.prescription.domain.MedicineDocument;
import com.healthapp.prescription.dto.MedicineSearchResult;
import com.healthapp.prescription.repository.MedicineCacheRepository;
import com.healthapp.prescription.repository.MedicineSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service for medicine search using Elasticsearch with fallback to local cache.
 * Disabled in test profile to avoid requiring Elasticsearch during tests.
 */
@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class MedicineSearchService {

    private final MedicineSearchRepository searchRepository;
    private final MedicineCacheRepository cacheRepository;
    private final ReactiveElasticsearchTemplate elasticsearchTemplate;

    /**
     * Search medicines with autocomplete.
     */
    public Flux<MedicineSearchResult> searchMedicines(String query, int limit) {
        log.debug("Searching medicines with query: {}", query);
        
        if (query == null || query.length() < 2) {
            return Flux.empty();
        }
        
        return searchInElasticsearch(query, limit)
                .onErrorResume(e -> {
                    log.warn("Elasticsearch search failed, falling back to cache: {}", e.getMessage());
                    return searchInCache(query, limit);
                });
    }

    private Flux<MedicineSearchResult> searchInElasticsearch(String query, int limit) {
        Criteria criteria = new Criteria("brandName").contains(query)
                .or("genericName").contains(query);
        
        CriteriaQuery searchQuery = new CriteriaQuery(criteria);
        searchQuery.setMaxResults(limit);
        
        return elasticsearchTemplate.search(searchQuery, MedicineDocument.class)
                .map(SearchHit::getContent)
                .map(this::toSearchResult);
    }

    private Flux<MedicineSearchResult> searchInCache(String query, int limit) {
        return cacheRepository.searchMedicines(query, limit)
                .map(cache -> MedicineSearchResult.builder()
                        .medicineId(cache.getMedicineId())
                        .brandName(cache.getBrandName())
                        .genericName(cache.getGenericName())
                        .manufacturer(cache.getManufacturer())
                        .category(cache.getCategory())
                        .formulation(cache.getFormulation())
                        .strength(cache.getStrength())
                        .packSize(cache.getPackSize())
                        .price(cache.getPrice())
                        .requiresPrescription(cache.getRequiresPrescription())
                        .isAvailable(cache.getIsAvailable())
                        .build());
    }

    /**
     * Get medicine by ID.
     */
    public Mono<MedicineSearchResult> getMedicineById(String medicineId) {
        return searchRepository.findById(medicineId)
                .map(this::toSearchResult)
                .switchIfEmpty(cacheRepository.findByMedicineId(medicineId)
                        .map(cache -> MedicineSearchResult.builder()
                                .medicineId(cache.getMedicineId())
                                .brandName(cache.getBrandName())
                                .genericName(cache.getGenericName())
                                .manufacturer(cache.getManufacturer())
                                .formulation(cache.getFormulation())
                                .strength(cache.getStrength())
                                .price(cache.getPrice())
                                .isAvailable(cache.getIsAvailable())
                                .build()));
    }

    /**
     * Find generic alternatives for a medicine.
     */
    public Flux<MedicineSearchResult> findAlternatives(String genericName) {
        return searchRepository.findByGenericNameContainingIgnoreCase(genericName)
                .filter(MedicineDocument::getIsAvailable)
                .map(this::toSearchResult);
    }

    /**
     * Get medicines by category.
     */
    public Flux<MedicineSearchResult> getMedicinesByCategory(String category) {
        return searchRepository.findByCategory(category)
                .map(this::toSearchResult);
    }

    private MedicineSearchResult toSearchResult(MedicineDocument doc) {
        return MedicineSearchResult.builder()
                .medicineId(doc.getMedicineId())
                .brandName(doc.getBrandName())
                .genericName(doc.getGenericName())
                .manufacturer(doc.getManufacturer())
                .category(doc.getCategory())
                .formulation(doc.getFormulation())
                .strength(doc.getStrength())
                .packSize(doc.getPackSize())
                .price(doc.getPrice())
                .requiresPrescription(doc.getRequiresPrescription())
                .isAvailable(doc.getIsAvailable())
                .alternativeIds(doc.getAlternativeIds())
                .build();
    }
}
