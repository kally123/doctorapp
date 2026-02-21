package com.healthapp.prescription.repository;

import com.healthapp.prescription.domain.MedicineDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@Profile("!test")
public interface MedicineSearchRepository extends ReactiveElasticsearchRepository<MedicineDocument, String> {

    Flux<MedicineDocument> findByBrandNameContainingIgnoreCase(String brandName);
    
    Flux<MedicineDocument> findByGenericNameContainingIgnoreCase(String genericName);
    
    Flux<MedicineDocument> findByCategory(String category);
    
    Flux<MedicineDocument> findByIsAvailableTrue();
}
