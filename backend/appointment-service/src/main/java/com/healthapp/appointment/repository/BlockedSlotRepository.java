package com.healthapp.appointment.repository;

import com.healthapp.appointment.domain.BlockedSlot;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface BlockedSlotRepository extends ReactiveCrudRepository<BlockedSlot, UUID> {
    
    Flux<BlockedSlot> findByDoctorId(UUID doctorId);
    
    @Query("""
        SELECT * FROM blocked_slots 
        WHERE doctor_id = :doctorId 
        AND start_datetime <= :endTime 
        AND end_datetime >= :startTime
        """)
    Flux<BlockedSlot> findOverlappingBlocks(UUID doctorId, Instant startTime, Instant endTime);
}
