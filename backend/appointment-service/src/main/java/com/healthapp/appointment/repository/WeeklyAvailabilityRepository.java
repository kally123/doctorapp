package com.healthapp.appointment.repository;

import com.healthapp.appointment.domain.WeeklyAvailability;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface WeeklyAvailabilityRepository extends ReactiveCrudRepository<WeeklyAvailability, UUID> {
    
    Flux<WeeklyAvailability> findByDoctorIdAndIsActive(UUID doctorId, Boolean isActive);
    
    Flux<WeeklyAvailability> findByDoctorId(UUID doctorId);
    
    @Query("SELECT * FROM weekly_availability WHERE doctor_id = :doctorId AND is_active = true")
    Flux<WeeklyAvailability> findActiveByDoctorId(UUID doctorId);
    
    @Query("DELETE FROM weekly_availability WHERE doctor_id = :doctorId")
    Mono<Void> deleteByDoctorId(UUID doctorId);
    
    @Query("""
        SELECT * FROM weekly_availability 
        WHERE doctor_id = :doctorId 
        AND day_of_week = :dayOfWeek 
        AND is_active = true
        """)
    Flux<WeeklyAvailability> findByDoctorIdAndDayOfWeek(UUID doctorId, Integer dayOfWeek);
}
