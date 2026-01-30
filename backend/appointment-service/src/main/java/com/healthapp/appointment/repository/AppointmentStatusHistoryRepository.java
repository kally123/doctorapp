package com.healthapp.appointment.repository;

import com.healthapp.appointment.domain.AppointmentStatusHistory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface AppointmentStatusHistoryRepository extends ReactiveCrudRepository<AppointmentStatusHistory, UUID> {
    
    Flux<AppointmentStatusHistory> findByAppointmentIdOrderByCreatedAtDesc(UUID appointmentId);
}
