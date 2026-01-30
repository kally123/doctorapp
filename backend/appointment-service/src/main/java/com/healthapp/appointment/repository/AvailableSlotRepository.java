package com.healthapp.appointment.repository;

import com.healthapp.appointment.domain.AvailableSlot;
import com.healthapp.appointment.domain.ConsultationType;
import com.healthapp.appointment.domain.SlotStatus;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface AvailableSlotRepository extends ReactiveCrudRepository<AvailableSlot, UUID> {
    
    @Query("""
        SELECT * FROM available_slots 
        WHERE doctor_id = :doctorId 
        AND slot_date >= :startDate 
        AND slot_date <= :endDate 
        AND status = 'AVAILABLE'
        ORDER BY slot_date, start_time
        """)
    Flux<AvailableSlot> findByDoctorIdAndDateRange(UUID doctorId, LocalDate startDate, LocalDate endDate);
    
    @Query("""
        SELECT * FROM available_slots 
        WHERE doctor_id = :doctorId 
        AND slot_date >= :startDate 
        AND slot_date <= :endDate 
        AND status = 'AVAILABLE'
        AND consultation_type = :consultationType
        ORDER BY slot_date, start_time
        """)
    Flux<AvailableSlot> findByDoctorIdAndDateRangeAndType(
            UUID doctorId, LocalDate startDate, LocalDate endDate, ConsultationType consultationType);
    
    @Modifying
    @Query("""
        UPDATE available_slots 
        SET status = :status, appointment_id = :appointmentId 
        WHERE id = :slotId
        """)
    Mono<Integer> updateStatus(UUID slotId, SlotStatus status, UUID appointmentId);
    
    @Modifying
    @Query("""
        DELETE FROM available_slots 
        WHERE doctor_id = :doctorId 
        AND slot_date >= :startDate 
        AND status = 'AVAILABLE'
        """)
    Mono<Integer> deleteByDoctorIdAndSlotDateAfterAndStatus(UUID doctorId, LocalDate startDate);
    
    @Query("""
        SELECT * FROM available_slots 
        WHERE doctor_id = :doctorId 
        AND slot_date = :slotDate 
        AND status = 'AVAILABLE'
        ORDER BY start_time
        """)
    Flux<AvailableSlot> findByDoctorIdAndSlotDate(UUID doctorId, LocalDate slotDate);
    
    Flux<AvailableSlot> findByDoctorId(UUID doctorId);
}
