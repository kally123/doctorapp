package com.healthapp.prescription.repository;

import org.springframework.context.annotation.Profile;
import com.healthapp.prescription.domain.Prescription;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Profile;
import java.time.LocalDate;
import java.util.UUID;

@Profile("!test")
@Repository
public interface PrescriptionRepository extends R2dbcRepository<Prescription, UUID> {

    Flux<Prescription> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    
    Flux<Prescription> findByDoctorIdOrderByCreatedAtDesc(UUID doctorId);
    
    Mono<Prescription> findByPrescriptionNumber(String prescriptionNumber);
    
    Mono<Prescription> findByConsultationId(UUID consultationId);
    
    Flux<Prescription> findByPatientIdAndStatus(UUID patientId, String status);
    
    Flux<Prescription> findByDoctorIdAndStatus(UUID doctorId, String status);
    
    @Query("SELECT * FROM prescriptions WHERE patient_id = :patientId AND prescription_date BETWEEN :startDate AND :endDate ORDER BY prescription_date DESC")
    Flux<Prescription> findByPatientIdAndDateRange(UUID patientId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT * FROM prescriptions WHERE doctor_id = :doctorId AND prescription_date = :date ORDER BY created_at DESC")
    Flux<Prescription> findByDoctorIdAndDate(UUID doctorId, LocalDate date);
    
    @Query("SELECT * FROM prescriptions WHERE status = 'SIGNED' AND valid_until < :date")
    Flux<Prescription> findExpiredPrescriptions(LocalDate date);
    
    Mono<Long> countByDoctorIdAndPrescriptionDate(UUID doctorId, LocalDate date);
}
