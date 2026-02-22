package com.healthapp.order.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.domain.TestPackage;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository for TestPackage entity.
 */
@Profile("!test")
@Repository
public interface TestPackageRepository extends R2dbcRepository<TestPackage, UUID> {

    Mono<TestPackage> findByPackageCode(String packageCode);

    Flux<TestPackage> findByIsActiveTrueOrderByDisplayOrderAsc();

    Flux<TestPackage> findByIsPopularTrueAndIsActiveTrue();

    Flux<TestPackage> findByTargetGenderAndIsActiveTrue(String targetGender);
}
