package com.healthapp.review.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.review.model.entity.DoctorRatingAggregate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Profile("!test")
@Repository
public interface RatingAggregateRepository extends R2dbcRepository<DoctorRatingAggregate, UUID> {

    Mono<DoctorRatingAggregate> findByDoctorId(UUID doctorId);

    @Query("""
        SELECT * FROM doctor_rating_aggregates 
        WHERE average_rating >= :minRating 
        ORDER BY average_rating DESC, total_reviews DESC 
        LIMIT :limit
        """)
    Flux<DoctorRatingAggregate> findTopRatedDoctors(BigDecimal minRating, int limit);

    @Query("""
        SELECT * FROM doctor_rating_aggregates 
        WHERE total_reviews >= :minReviews 
        ORDER BY total_reviews DESC 
        LIMIT :limit
        """)
    Flux<DoctorRatingAggregate> findMostReviewedDoctors(int minReviews, int limit);
}
