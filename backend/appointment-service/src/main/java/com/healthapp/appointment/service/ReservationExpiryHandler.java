package com.healthapp.appointment.service;

import com.healthapp.appointment.domain.Appointment;
import com.healthapp.appointment.domain.AppointmentStatus;
import com.healthapp.appointment.domain.SlotStatus;
import com.healthapp.appointment.event.AppointmentEventPublisher;
import com.healthapp.appointment.repository.AppointmentRepository;
import com.healthapp.appointment.repository.AvailableSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpiryHandler {
    
    private final AppointmentRepository appointmentRepo;
    private final AvailableSlotRepository slotRepo;
    private final AppointmentEventPublisher eventPublisher;
    
    @Scheduled(fixedRate = 60000) // Run every minute
    public void handleExpiredReservations() {
        log.debug("Checking for expired reservations");
        
        appointmentRepo.findExpiredReservations(Instant.now())
                .flatMap(this::releaseReservation)
                .subscribe(
                        appointment -> log.info("Released expired reservation: {}", appointment.getId()),
                        error -> log.error("Error releasing reservation", error)
                );
    }
    
    private reactor.core.publisher.Mono<Appointment> releaseReservation(Appointment appointment) {
        return reactor.core.publisher.Mono.defer(() -> {
            Appointment updated = appointment.toBuilder()
                    .status(AppointmentStatus.CANCELLED_SYSTEM)
                    .cancellationReason("Reservation expired - payment not completed")
                    .cancelledAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            
            return appointmentRepo.save(updated);
        })
        .flatMap(updated ->
                slotRepo.updateStatus(updated.getSlotId(), SlotStatus.AVAILABLE, null)
                        .thenReturn(updated))
        .doOnSuccess(updated -> eventPublisher.publishExpired(updated));
    }
}
