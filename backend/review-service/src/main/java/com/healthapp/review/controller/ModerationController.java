package com.healthapp.review.controller;

import org.springframework.context.annotation.Profile;
import com.healthapp.review.dto.ModerationRequest;
import com.healthapp.review.dto.ReportReviewRequest;
import com.healthapp.review.dto.ReviewResponse;
import com.healthapp.review.service.ModerationService;
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

@Profile("!test")
@RestController
@RequestMapping("/api/v1/moderation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Moderation", description = "Review moderation APIs")
public class ModerationController {

    private final ModerationService moderationService;

    @GetMapping("/reviews/pending")
    @Operation(summary = "Get pending reviews for moderation")
    public Flux<ReviewResponse> getPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return moderationService.getPendingReviews(page, size);
    }

    @GetMapping("/reviews/flagged")
    @Operation(summary = "Get flagged reviews for moderation")
    public Flux<ReviewResponse> getFlaggedReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return moderationService.getFlaggedReviews(page, size);
    }

    @PutMapping("/reviews/{reviewId}")
    @Operation(summary = "Moderate a review")
    public Mono<ReviewResponse> moderateReview(
            @RequestHeader("X-User-Id") UUID moderatorId,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ModerationRequest request) {
        
        return moderationService.moderateReview(moderatorId, reviewId, request);
    }

    @PostMapping("/reviews/{reviewId}/report")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Report a review")
    public Mono<Void> reportReview(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReportReviewRequest request) {
        
        return moderationService.reportReview(userId, reviewId, request);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get moderation statistics")
    public Mono<ModerationService.ModerationStats> getModerationStats() {
        return moderationService.getModerationStats();
    }
}
