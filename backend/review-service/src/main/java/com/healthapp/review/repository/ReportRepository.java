package com.healthapp.review.repository;

import com.healthapp.review.model.entity.ReviewReport;
import com.healthapp.review.model.enums.ReportStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ReportRepository extends R2dbcRepository<ReviewReport, UUID> {

    Flux<ReviewReport> findByReviewId(UUID reviewId);

    Flux<ReviewReport> findByStatusOrderByCreatedAtAsc(ReportStatus status, Pageable pageable);

    Mono<Boolean> existsByReviewIdAndReporterId(UUID reviewId, UUID reporterId);

    Mono<Long> countByReviewId(UUID reviewId);

    Mono<Long> countByStatus(ReportStatus status);
}
