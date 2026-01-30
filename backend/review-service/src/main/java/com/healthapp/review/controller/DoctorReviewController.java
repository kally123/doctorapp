package com.healthapp.review.controller;

import com.healthapp.review.dto.*;
import com.healthapp.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors/{doctorId}/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Doctor Reviews", description = "Doctor-specific review APIs")
public class DoctorReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "Get reviews for a doctor")
    public Flux<ReviewResponse> getDoctorReviews(
            @PathVariable UUID doctorId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Integer maxRating,
            @RequestParam(required = false) String consultationType,
            @RequestParam(defaultValue = "recent") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        ReviewFilter filter = ReviewFilter.builder()
                .minRating(minRating)
                .maxRating(maxRating)
                .sortBy(sortBy)
                .build();
        
        return reviewService.getDoctorReviews(doctorId, filter, page, size);
    }

    @GetMapping("/rating")
    @Operation(summary = "Get doctor's rating aggregate")
    public Mono<DoctorRatingResponse> getDoctorRating(@PathVariable UUID doctorId) {
        return reviewService.getDoctorRating(doctorId);
    }

    @PostMapping("/{reviewId}/respond")
    @Operation(summary = "Doctor responds to a review")
    public Mono<ReviewResponse> respondToReview(
            @RequestHeader("X-User-Id") UUID doctorId,
            @PathVariable("doctorId") UUID pathDoctorId,
            @PathVariable UUID reviewId,
            @Valid @RequestBody DoctorResponseRequest request) {
        
        // Verify the doctor is responding to their own reviews
        if (!doctorId.equals(pathDoctorId)) {
            return Mono.error(new IllegalArgumentException("Unauthorized"));
        }
        
        return reviewService.respondToReview(doctorId, reviewId, request);
    }
}
