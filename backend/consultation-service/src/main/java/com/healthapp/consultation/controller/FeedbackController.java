package com.healthapp.consultation.controller;

import com.healthapp.consultation.domain.ConsultationFeedback;
import com.healthapp.consultation.dto.SubmitFeedbackRequest;
import com.healthapp.consultation.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for consultation feedback.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback", description = "Consultation feedback APIs")
public class FeedbackController {
    
    private final FeedbackService feedbackService;
    
    @PostMapping
    @Operation(summary = "Submit feedback", description = "Submits feedback for a consultation")
    public Mono<ResponseEntity<ConsultationFeedback>> submitFeedback(
            @RequestHeader("X-User-Id") UUID patientId,
            @Valid @RequestBody SubmitFeedbackRequest request) {
        return feedbackService.submitFeedback(patientId, request)
                .map(feedback -> ResponseEntity.status(HttpStatus.CREATED).body(feedback))
                .doOnSuccess(r -> log.info("Feedback submitted for session: {}", request.getSessionId()));
    }
    
    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get session feedback", description = "Gets feedback for a specific session")
    public Mono<ResponseEntity<ConsultationFeedback>> getSessionFeedback(@PathVariable UUID sessionId) {
        return feedbackService.getFeedback(sessionId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get doctor feedback", description = "Gets all feedback for a doctor")
    public Flux<ConsultationFeedback> getDoctorFeedback(@PathVariable UUID doctorId) {
        return feedbackService.getDoctorFeedback(doctorId);
    }
    
    @GetMapping("/doctor/{doctorId}/summary")
    @Operation(summary = "Get doctor rating summary", description = "Gets average ratings for a doctor")
    public Mono<ResponseEntity<FeedbackService.DoctorRatingSummary>> getDoctorRatingSummary(
            @PathVariable UUID doctorId) {
        return feedbackService.getDoctorRatingSummary(doctorId)
                .map(ResponseEntity::ok);
    }
}
