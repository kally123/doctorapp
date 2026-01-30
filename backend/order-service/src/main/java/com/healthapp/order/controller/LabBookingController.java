package com.healthapp.order.controller;

import com.healthapp.order.dto.*;
import com.healthapp.order.domain.enums.LabBookingStatus;
import com.healthapp.order.service.LabBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST controller for lab booking management.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/lab-bookings")
@RequiredArgsConstructor
@Tag(name = "Lab Bookings", description = "Lab test booking management APIs")
public class LabBookingController {

    private final LabBookingService labBookingService;

    @GetMapping("/slots")
    @Operation(summary = "Get available slots", description = "Get available collection slots for a lab partner")
    public Flux<AvailableSlotResponse> getAvailableSlots(
            @RequestParam UUID labPartnerId,
            @RequestParam LocalDate date,
            @RequestParam(required = false) String pincode) {
        log.info("Getting available slots for lab: {} on date: {}", labPartnerId, date);
        return labBookingService.getAvailableSlots(labPartnerId, date, pincode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create booking", description = "Create a new lab test booking")
    public Mono<ResponseEntity<LabBookingResponse>> createBooking(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateLabBookingRequest request) {
        log.info("Creating lab booking for user: {}", userId);
        return labBookingService.createBooking(userId, request)
                .map(booking -> ResponseEntity.status(HttpStatus.CREATED).body(booking));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking", description = "Get lab booking details by ID")
    public Mono<ResponseEntity<LabBookingResponse>> getBooking(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID bookingId) {
        log.info("Getting lab booking: {} for user: {}", bookingId, userId);
        return labBookingService.getBooking(bookingId)
                .filter(booking -> booking.getUserId().equals(userId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{bookingNumber}")
    @Operation(summary = "Get booking by number", description = "Get lab booking details by booking number")
    public Mono<ResponseEntity<LabBookingResponse>> getBookingByNumber(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable String bookingNumber) {
        log.info("Getting lab booking by number: {} for user: {}", bookingNumber, userId);
        return labBookingService.getBookingByNumber(bookingNumber)
                .filter(booking -> booking.getUserId().equals(userId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get user bookings", description = "Get all lab bookings for the current user")
    public Flux<LabBookingResponse> getUserBookings(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) LabBookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting lab bookings for user: {}, status: {}", userId, status);
        if (status != null) {
            return labBookingService.getUserBookingsByStatus(userId, status, page, size);
        }
        return labBookingService.getUserBookings(userId, page, size);
    }

    @PostMapping("/{bookingId}/confirm-payment")
    @Operation(summary = "Confirm payment", description = "Confirm payment for a lab booking")
    public Mono<ResponseEntity<LabBookingResponse>> confirmPayment(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID bookingId,
            @RequestParam String paymentId,
            @RequestParam String transactionId) {
        log.info("Confirming payment for lab booking: {}, payment: {}", bookingId, paymentId);
        return labBookingService.getBooking(bookingId)
                .filter(booking -> booking.getUserId().equals(userId))
                .flatMap(booking -> labBookingService.confirmPayment(bookingId, paymentId, transactionId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel booking", description = "Cancel a lab booking")
    public Mono<ResponseEntity<LabBookingResponse>> cancelBooking(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID bookingId,
            @RequestParam String reason) {
        log.info("Cancelling lab booking: {} for user: {}, reason: {}", bookingId, userId, reason);
        return labBookingService.getBooking(bookingId)
                .filter(booking -> booking.getUserId().equals(userId))
                .flatMap(booking -> labBookingService.cancelBooking(bookingId, reason))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{bookingId}/reschedule")
    @Operation(summary = "Reschedule booking", description = "Reschedule a lab booking to a new slot")
    public Mono<ResponseEntity<LabBookingResponse>> rescheduleBooking(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID bookingId,
            @RequestParam LocalDate newDate,
            @RequestParam UUID newSlotId) {
        log.info("Rescheduling lab booking: {} to date: {}", bookingId, newDate);
        return labBookingService.getBooking(bookingId)
                .filter(booking -> booking.getUserId().equals(userId))
                .flatMap(booking -> labBookingService.rescheduleBooking(bookingId, newDate, newSlotId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Partner endpoints
    @PutMapping("/{bookingId}/status")
    @Operation(summary = "Update booking status", description = "Update lab booking status (Partner only)")
    public Mono<ResponseEntity<LabBookingResponse>> updateBookingStatus(
            @RequestHeader("X-Partner-Id") UUID partnerId,
            @PathVariable UUID bookingId,
            @RequestParam LabBookingStatus status,
            @RequestParam(required = false) String notes) {
        log.info("Updating lab booking: {} status to: {} by partner: {}", bookingId, status, partnerId);
        return labBookingService.updateBookingStatus(bookingId, status, notes)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{bookingId}/upload-report")
    @Operation(summary = "Upload lab report", description = "Upload lab test report PDF (Partner only)")
    public Mono<ResponseEntity<LabBookingResponse>> uploadReport(
            @RequestHeader("X-Partner-Id") UUID partnerId,
            @PathVariable UUID bookingId,
            @RequestPart("file") FilePart file) {
        log.info("Uploading report for lab booking: {} by partner: {}", bookingId, partnerId);
        return labBookingService.uploadReport(bookingId, file)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/partner")
    @Operation(summary = "Get partner bookings", description = "Get all lab bookings assigned to a partner")
    public Flux<LabBookingResponse> getPartnerBookings(
            @RequestHeader("X-Partner-Id") UUID partnerId,
            @RequestParam(required = false) LabBookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting lab bookings for partner: {}, status: {}", partnerId, status);
        return labBookingService.getPartnerBookings(partnerId, status, page, size);
    }

    @PutMapping("/{bookingId}/assign-phlebotomist")
    @Operation(summary = "Assign phlebotomist", description = "Assign a phlebotomist for home collection (Partner only)")
    public Mono<ResponseEntity<LabBookingResponse>> assignPhlebotomist(
            @RequestHeader("X-Partner-Id") UUID partnerId,
            @PathVariable UUID bookingId,
            @RequestParam UUID phlebotomistId) {
        log.info("Assigning phlebotomist: {} to booking: {}", phlebotomistId, bookingId);
        return labBookingService.assignPhlebotomist(bookingId, phlebotomistId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
