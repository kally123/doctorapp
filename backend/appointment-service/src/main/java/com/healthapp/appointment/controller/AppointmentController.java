package com.healthapp.appointment.controller;

import com.healthapp.appointment.domain.AppointmentStatus;
import com.healthapp.appointment.dto.*;
import com.healthapp.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    @PostMapping("/reserve")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ReservationResponse> reserveSlot(
            @RequestHeader("X-User-Id") String patientId,
            @Valid @RequestBody BookingRequest request) {
        return appointmentService.reserveSlot(UUID.fromString(patientId), request);
    }
    
    @PostMapping("/{appointmentId}/confirm")
    public Mono<AppointmentDto> confirmAppointment(
            @RequestHeader("X-User-Id") String patientId,
            @PathVariable String appointmentId,
            @Valid @RequestBody PaymentConfirmation paymentConfirmation) {
        return appointmentService.confirmAppointment(
                UUID.fromString(patientId),
                UUID.fromString(appointmentId),
                paymentConfirmation
        );
    }
    
    @PostMapping("/{appointmentId}/cancel")
    public Mono<AppointmentDto> cancelAppointment(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String appointmentId,
            @Valid @RequestBody CancellationRequest request) {
        return appointmentService.cancelAppointment(
                UUID.fromString(userId),
                UUID.fromString(appointmentId),
                request
        );
    }
    
    @GetMapping("/{appointmentId}")
    public Mono<AppointmentDto> getAppointment(
            @PathVariable String appointmentId) {
        return appointmentService.getAppointment(UUID.fromString(appointmentId));
    }
    
    @GetMapping("/patient/me")
    public Flux<AppointmentDto> getPatientAppointments(
            @RequestHeader("X-User-Id") String patientId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return appointmentService.getPatientAppointments(
                UUID.fromString(patientId), status, fromDate, page, size);
    }
    
    @GetMapping("/doctor/me")
    public Flux<AppointmentDto> getDoctorAppointments(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) AppointmentStatus status) {
        return appointmentService.getDoctorAppointments(UUID.fromString(userId), date, status);
    }
}
