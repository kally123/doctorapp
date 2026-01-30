package com.healthapp.appointment.service;

import com.healthapp.appointment.domain.*;
import com.healthapp.appointment.dto.*;
import com.healthapp.appointment.event.AppointmentEventPublisher;
import com.healthapp.appointment.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepo;
    private final AvailableSlotRepository slotRepo;
    private final AppointmentStatusHistoryRepository historyRepo;
    private final AppointmentEventPublisher eventPublisher;
    
    @Value("${appointment.reservation.expiry-minutes:10}")
    private int reservationExpiryMinutes;
    
    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = new BigDecimal("0.10"); // 10%
    
    @Transactional
    public Mono<ReservationResponse> reserveSlot(UUID patientId, BookingRequest request) {
        return slotRepo.findById(request.getSlotId())
                .switchIfEmpty(Mono.error(new RuntimeException("Slot not found")))
                .flatMap(this::validateSlotAvailable)
                .flatMap(slot -> createReservation(patientId, slot, request))
                .flatMap(this::updateSlotToReserved)
                .flatMap(this::saveStatusHistory)
                .doOnSuccess(appointment -> eventPublisher.publishReserved(appointment))
                .map(this::toReservationResponse);
    }
    
    private Mono<AvailableSlot> validateSlotAvailable(AvailableSlot slot) {
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            return Mono.error(new RuntimeException("Slot is no longer available"));
        }
        if (slot.getSlotDate().isBefore(LocalDate.now())) {
            return Mono.error(new RuntimeException("Cannot book past slots"));
        }
        return Mono.just(slot);
    }
    
    private Mono<Appointment> createReservation(UUID patientId, AvailableSlot slot, BookingRequest request) {
        // For now, use a fixed fee. In production, this would come from doctor-service
        BigDecimal consultationFee = new BigDecimal("500");
        BigDecimal platformFee = consultationFee.multiply(PLATFORM_FEE_PERCENTAGE);
        BigDecimal totalAmount = consultationFee.add(platformFee);
        
        Instant scheduledAt = slot.getSlotDate()
                .atTime(slot.getStartTime())
                .atZone(ZoneId.systemDefault())
                .toInstant();
        
        Appointment appointment = Appointment.builder()
                .patientId(patientId)
                .doctorId(request.getDoctorId())
                .clinicId(request.getClinicId())
                .slotId(slot.getId())
                .scheduledAt(scheduledAt)
                .durationMinutes(slot.getSlotDurationMinutes())
                .consultationType(request.getConsultationType())
                .status(AppointmentStatus.PENDING_PAYMENT)
                .consultationFee(consultationFee)
                .platformFee(platformFee)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .currency("INR")
                .bookingNotes(request.getBookingNotes())
                .reservedUntil(Instant.now().plusSeconds(reservationExpiryMinutes * 60L))
                .isFollowup(false)
                .rescheduleCount(0)
                .followupScheduled(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        return appointmentRepo.save(appointment);
    }
    
    private Mono<Appointment> updateSlotToReserved(Appointment appointment) {
        return slotRepo.updateStatus(appointment.getSlotId(), SlotStatus.RESERVED, appointment.getId())
                .thenReturn(appointment);
    }
    
    private Mono<Appointment> saveStatusHistory(Appointment appointment) {
        AppointmentStatusHistory history = AppointmentStatusHistory.builder()
                .appointmentId(appointment.getId())
                .fromStatus(null)
                .toStatus(AppointmentStatus.PENDING_PAYMENT)
                .changedBy(appointment.getPatientId())
                .reason("Slot reserved")
                .createdAt(Instant.now())
                .build();
        
        return historyRepo.save(history).thenReturn(appointment);
    }
    
    @Transactional
    public Mono<AppointmentDto> confirmAppointment(UUID patientId, UUID appointmentId, PaymentConfirmation payment) {
        return appointmentRepo.findById(appointmentId)
                .switchIfEmpty(Mono.error(new RuntimeException("Appointment not found")))
                .flatMap(appointment -> validateConfirmation(appointment, patientId))
                .flatMap(appointment -> {
                    Appointment updated = appointment.toBuilder()
                            .status(AppointmentStatus.CONFIRMED)
                            .paymentId(payment.getPaymentId())
                            .paymentStatus(payment.getStatus())
                            .reservedUntil(null)
                            .updatedAt(Instant.now())
                            .build();
                    
                    return appointmentRepo.save(updated);
                })
                .flatMap(appointment -> 
                        slotRepo.updateStatus(appointment.getSlotId(), SlotStatus.BOOKED, appointment.getId())
                                .thenReturn(appointment))
                .flatMap(appointment -> saveStatusTransition(appointment, 
                        AppointmentStatus.PENDING_PAYMENT, AppointmentStatus.CONFIRMED, patientId))
                .doOnSuccess(appointment -> eventPublisher.publishConfirmed(appointment))
                .map(this::toDto);
    }
    
    private Mono<Appointment> validateConfirmation(Appointment appointment, UUID patientId) {
        if (!appointment.getPatientId().equals(patientId)) {
            return Mono.error(new RuntimeException("Not authorized"));
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            return Mono.error(new RuntimeException("Invalid appointment status"));
        }
        if (appointment.getReservedUntil() != null && appointment.getReservedUntil().isBefore(Instant.now())) {
            return Mono.error(new RuntimeException("Reservation expired"));
        }
        return Mono.just(appointment);
    }
    
    @Transactional
    public Mono<AppointmentDto> cancelAppointment(UUID userId, UUID appointmentId, CancellationRequest request) {
        return appointmentRepo.findById(appointmentId)
                .switchIfEmpty(Mono.error(new RuntimeException("Appointment not found")))
                .flatMap(appointment -> validateCancellation(appointment, userId))
                .flatMap(appointment -> {
                    AppointmentStatus cancelStatus = appointment.getPatientId().equals(userId)
                            ? AppointmentStatus.CANCELLED_BY_PATIENT
                            : AppointmentStatus.CANCELLED_BY_DOCTOR;
                    
                    Appointment updated = appointment.toBuilder()
                            .status(cancelStatus)
                            .cancelledAt(Instant.now())
                            .cancelledBy(userId)
                            .cancellationReason(request.getReason())
                            .updatedAt(Instant.now())
                            .build();
                    
                    return appointmentRepo.save(updated);
                })
                .flatMap(appointment -> 
                        slotRepo.updateStatus(appointment.getSlotId(), SlotStatus.AVAILABLE, null)
                                .thenReturn(appointment))
                .doOnSuccess(appointment -> eventPublisher.publishCancelled(appointment))
                .map(this::toDto);
    }
    
    private Mono<Appointment> validateCancellation(Appointment appointment, UUID userId) {
        if (!appointment.getPatientId().equals(userId) && !appointment.getDoctorId().equals(userId)) {
            return Mono.error(new RuntimeException("Not authorized"));
        }
        if (!appointment.getStatus().isCancellable()) {
            return Mono.error(new RuntimeException("Appointment cannot be cancelled in current status"));
        }
        return Mono.just(appointment);
    }
    
    public Mono<Void> releaseReservation(UUID appointmentId) {
        return appointmentRepo.findById(appointmentId)
                .flatMap(appointment -> {
                    Appointment updated = appointment.toBuilder()
                            .status(AppointmentStatus.CANCELLED_SYSTEM)
                            .cancellationReason("Reservation expired")
                            .cancelledAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    return appointmentRepo.save(updated);
                })
                .flatMap(appointment -> 
                        slotRepo.updateStatus(appointment.getSlotId(), SlotStatus.AVAILABLE, null)
                                .thenReturn(appointment))
                .doOnSuccess(appointment -> eventPublisher.publishExpired(appointment))
                .then();
    }
    
    public Flux<AppointmentDto> getPatientAppointments(UUID patientId, AppointmentStatus status, 
                                                        LocalDate fromDate, int page, int size) {
        int offset = page * size;
        
        if (status != null) {
            return appointmentRepo.findByPatientIdAndStatusIn(
                    patientId, new String[]{status.name()}, size, offset)
                    .map(this::toDto);
        }
        
        return appointmentRepo.findByPatientId(patientId, size, offset)
                .map(this::toDto);
    }
    
    public Flux<AppointmentDto> getDoctorAppointments(UUID doctorId, LocalDate date, AppointmentStatus status) {
        if (date != null && status != null) {
            return appointmentRepo.findByDoctorIdAndStatusInAndDate(
                    doctorId, new String[]{status.name()}, date)
                    .map(this::toDto);
        }
        
        if (date != null) {
            return appointmentRepo.findByDoctorIdAndDate(doctorId, date)
                    .map(this::toDto);
        }
        
        return appointmentRepo.findByDoctorId(doctorId)
                .map(this::toDto);
    }
    
    public Mono<AppointmentDto> getAppointment(UUID appointmentId) {
        return appointmentRepo.findById(appointmentId)
                .map(this::toDto);
    }
    
    private Mono<Appointment> saveStatusTransition(Appointment appointment, 
                                                    AppointmentStatus fromStatus, 
                                                    AppointmentStatus toStatus, 
                                                    UUID changedBy) {
        AppointmentStatusHistory history = AppointmentStatusHistory.builder()
                .appointmentId(appointment.getId())
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .createdAt(Instant.now())
                .build();
        
        return historyRepo.save(history).thenReturn(appointment);
    }
    
    private ReservationResponse toReservationResponse(Appointment appointment) {
        return ReservationResponse.builder()
                .appointmentId(appointment.getId())
                .doctorId(appointment.getDoctorId())
                .scheduledAt(appointment.getScheduledAt())
                .consultationType(appointment.getConsultationType())
                .consultationFee(appointment.getConsultationFee())
                .platformFee(appointment.getPlatformFee())
                .totalAmount(appointment.getTotalAmount())
                .currency(appointment.getCurrency())
                .reservedUntil(appointment.getReservedUntil())
                .expiresInSeconds(reservationExpiryMinutes * 60L)
                .build();
    }
    
    private AppointmentDto toDto(Appointment appointment) {
        return AppointmentDto.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .clinicId(appointment.getClinicId())
                .scheduledAt(appointment.getScheduledAt())
                .durationMinutes(appointment.getDurationMinutes())
                .consultationType(appointment.getConsultationType())
                .status(appointment.getStatus())
                .consultationFee(appointment.getConsultationFee())
                .platformFee(appointment.getPlatformFee())
                .discountAmount(appointment.getDiscountAmount())
                .totalAmount(appointment.getTotalAmount())
                .currency(appointment.getCurrency())
                .paymentStatus(appointment.getPaymentStatus())
                .bookingNotes(appointment.getBookingNotes())
                .cancelledAt(appointment.getCancelledAt())
                .cancellationReason(appointment.getCancellationReason())
                .refundAmount(appointment.getRefundAmount())
                .refundStatus(appointment.getRefundStatus())
                .isFollowup(appointment.getIsFollowup())
                .rescheduleCount(appointment.getRescheduleCount())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
