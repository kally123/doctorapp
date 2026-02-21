package com.healthapp.appointment.repository;

import com.healthapp.appointment.domain.Appointment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends ReactiveCrudRepository<Appointment, UUID> {
    
    @Query("""
        SELECT * FROM appointments 
        WHERE status = 'PENDING_PAYMENT' 
        AND reserved_until < :now
        """)
    Flux<Appointment> findExpiredReservations(Instant now);
    
    @Query("""
        SELECT * FROM appointments 
        WHERE patient_id = :patientId 
        ORDER BY scheduled_at DESC
        LIMIT :size OFFSET :offset
        """)
    Flux<Appointment> findByPatientId(UUID patientId, int size, int offset);
    
    @Query("""
        SELECT * FROM appointments 
        WHERE patient_id = :patientId 
        AND status = ANY(:statuses)
        ORDER BY scheduled_at DESC
        LIMIT :size OFFSET :offset
        """)
    Flux<Appointment> findByPatientIdAndStatusIn(UUID patientId, String[] statuses, int size, int offset);
    
    @Query("""
        SELECT * FROM appointments 
        WHERE doctor_id = :doctorId 
        ORDER BY scheduled_at ASC
        """)
    Flux<Appointment> findByDoctorId(UUID doctorId);
    
    @Query("""
        SELECT * FROM appointments 
        WHERE doctor_id = :doctorId 
        AND DATE(scheduled_at) = :date
        ORDER BY scheduled_at ASC
        """)
    Flux<Appointment> findByDoctorIdAndDate(UUID doctorId, LocalDate date);
    
    @Query("""
        SELECT * FROM appointments 
        WHERE doctor_id = :doctorId 
        AND status = ANY(:statuses)
        AND DATE(scheduled_at) = :date
        ORDER BY scheduled_at ASC
        """)
    Flux<Appointment> findByDoctorIdAndStatusInAndDate(UUID doctorId, String[] statuses, LocalDate date);
    
    Mono<Appointment> findBySlotId(UUID slotId);
    
    @Query("""
        SELECT * FROM appointments 
        WHERE scheduled_at BETWEEN :start AND :end 
        AND status IN ('CONFIRMED', 'REMINDER_SENT')
        """)
    Flux<Appointment> findUpcomingAppointments(Instant start, Instant end);
    
    @Query("SELECT COUNT(*) FROM appointments WHERE patient_id = :patientId AND doctor_id = :doctorId")
    Mono<Long> countByPatientIdAndDoctorId(UUID patientId, UUID doctorId);
}
