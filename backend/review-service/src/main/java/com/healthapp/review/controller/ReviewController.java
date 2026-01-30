package com.healthapp.review.controller;

import com.healthapp.review.dto.*;
import com.healthapp.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reviews", description = "Review management APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a new review")
    public Mono<ReviewResponse> submitReview(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody SubmitReviewRequest request) {
        
        log.info("User {} submitting review for consultation {}", 
                userId, request.getConsultationId());
        return reviewService.submitReview(userId, request);
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get a review by ID")
    public Mono<ReviewResponse> getReview(@PathVariable UUID reviewId) {
        return reviewService.getReviewById(reviewId);
    }

    @GetMapping("/my-reviews")
    @Operation(summary = "Get current user's reviews")
    public Flux<ReviewResponse> getMyReviews(@RequestHeader("X-User-Id") UUID userId) {
        return reviewService.getPatientReviews(userId);
    }

    @PostMapping("/{reviewId}/vote")
    @Operation(summary = "Vote a review as helpful or not helpful")
    public Mono<Void> voteReview(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID reviewId,
            @Valid @RequestBody VoteRequest request) {
        
        return reviewService.voteReview(userId, reviewId, request.getVoteType());
    }
}
