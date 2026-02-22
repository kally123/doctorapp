package com.healthapp.review.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.review.model.entity.ReviewVote;
import com.healthapp.review.model.enums.VoteType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Profile("!test")
@Repository
public interface VoteRepository extends R2dbcRepository<ReviewVote, UUID> {

    Mono<ReviewVote> findByReviewIdAndUserId(UUID reviewId, UUID userId);

    Flux<ReviewVote> findByReviewId(UUID reviewId);

    Mono<Boolean> existsByReviewIdAndUserId(UUID reviewId, UUID userId);

    @Query("SELECT COUNT(*) FROM review_votes WHERE review_id = :reviewId AND vote_type = :voteType")
    Mono<Integer> countByReviewIdAndVoteType(UUID reviewId, VoteType voteType);

    Mono<Void> deleteByReviewIdAndUserId(UUID reviewId, UUID userId);
}
