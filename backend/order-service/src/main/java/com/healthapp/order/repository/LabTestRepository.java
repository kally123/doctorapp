package com.healthapp.order.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.domain.LabTest;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository for LabTest entity.
 */
@Profile("!test")
@Repository
public interface LabTestRepository extends R2dbcRepository<LabTest, UUID> {

    Mono<LabTest> findByTestCode(String testCode);

    Flux<LabTest> findByCategoryIdAndIsActiveTrue(UUID categoryId);

    Flux<LabTest> findByIsPopularTrueAndIsActiveTrue();

    @Query("SELECT * FROM lab_tests WHERE is_active = true ORDER BY name ASC LIMIT :limit OFFSET :offset")
    Flux<LabTest> findAllActiveWithPagination(int limit, int offset);

    @Query("""
        SELECT * FROM lab_tests 
        WHERE is_active = true 
        AND (
            name ILIKE '%' || :query || '%' 
            OR short_name ILIKE '%' || :query || '%'
            OR :query = ANY(keywords)
        )
        ORDER BY is_popular DESC, name ASC
        LIMIT :limit
        """)
    Flux<LabTest> searchTests(String query, int limit);

    Mono<Long> countByIsActiveTrue();
}
