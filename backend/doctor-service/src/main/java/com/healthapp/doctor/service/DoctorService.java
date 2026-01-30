package com.healthapp.doctor.service;

import com.healthapp.common.dto.PageRequest;
import com.healthapp.common.dto.PageResponse;
import com.healthapp.common.exception.ConflictException;
import com.healthapp.common.exception.NotFoundException;
import com.healthapp.doctor.event.DoctorEventPublisher;
import com.healthapp.doctor.mapper.DoctorMapper;
import com.healthapp.doctor.model.dto.*;
import com.healthapp.doctor.model.entity.*;
import com.healthapp.doctor.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing doctor profiles and related operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorService {
    
    private final DoctorRepository doctorRepository;
    private final SpecializationRepository specializationRepository;
    private final QualificationRepository qualificationRepository;
    private final LanguageRepository languageRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorMapper doctorMapper;
    private final DoctorEventPublisher eventPublisher;
    private final R2dbcEntityTemplate entityTemplate;
    
    /**
     * Creates a new doctor profile.
     */
    @Transactional
    public Mono<DoctorDto> createDoctor(UUID userId, CreateDoctorRequest request) {
        log.info("Creating doctor profile for user: {}", userId);
        
        return doctorRepository.existsByUserId(userId)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException("Doctor profile already exists for this user"));
                    }
                    return doctorRepository.existsByRegistrationNumber(request.getRegistrationNumber());
                })
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException("Registration number already exists"));
                    }
                    
                    Doctor doctor = doctorMapper.toEntity(request);
                    doctor.setUserId(userId);
                    
                    return doctorRepository.save(doctor);
                })
                .flatMap(doctor -> {
                    // Link specializations - convert String IDs to UUIDs
                    List<UUID> specUuids = request.getSpecializationIds() != null 
                            ? request.getSpecializationIds().stream().map(UUID::fromString).toList() 
                            : List.of();
                    Mono<Void> specializationsSaved = linkSpecializations(doctor.getId(), specUuids, null);
                    
                    // Link languages - convert String IDs to UUIDs  
                    List<UUID> langUuids = request.getLanguageIds() != null
                            ? request.getLanguageIds().stream().map(UUID::fromString).toList()
                            : List.of();
                    Mono<Void> languagesSaved = linkLanguages(doctor.getId(), langUuids);
                    
                    return Mono.when(specializationsSaved, languagesSaved)
                            .then(Mono.just(doctor));
                })
                .flatMap(doctor -> 
                    eventPublisher.publishDoctorCreated(doctor)
                            .then(enrichDoctorDto(doctor))
                )
                .doOnSuccess(dto -> log.info("Doctor profile created: {}", dto.getId()));
    }
    
    /**
     * Gets a doctor by ID with all related data.
     */
    public Mono<DoctorDto> getDoctorById(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .switchIfEmpty(Mono.error(new NotFoundException("Doctor not found")))
                .flatMap(this::enrichDoctorDto)
                .doOnSuccess(doctor -> incrementProfileViews(doctorId).subscribe());
    }
    
    /**
     * Gets a doctor by user ID.
     */
    public Mono<DoctorDto> getDoctorByUserId(UUID userId) {
        return doctorRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("Doctor profile not found for user")))
                .flatMap(this::enrichDoctorDto);
    }
    
    /**
     * Updates a doctor profile.
     */
    @Transactional
    public Mono<DoctorDto> updateDoctor(UUID doctorId, UpdateDoctorRequest request) {
        log.info("Updating doctor profile: {}", doctorId);
        
        return doctorRepository.findById(doctorId)
                .switchIfEmpty(Mono.error(new NotFoundException("Doctor not found")))
                .map(doctor -> doctorMapper.updateEntity(doctor, request))
                .flatMap(doctorRepository::save)
                .flatMap(doctor -> 
                    eventPublisher.publishDoctorUpdated(doctor)
                            .then(enrichDoctorDto(doctor))
                )
                .doOnSuccess(dto -> log.info("Doctor profile updated: {}", dto.getId()));
    }
    
    /**
     * Gets top rated doctors.
     */
    public Flux<DoctorDto> getTopDoctors(int limit) {
        return doctorRepository.findTopDoctors(limit)
                .flatMap(this::enrichDoctorDto);
    }
    
    /**
     * Gets doctors by city.
     */
    public Mono<PageResponse<DoctorDto>> getDoctorsByCity(String city, PageRequest pageRequest) {
        return doctorRepository.findByCity(city)
                .flatMap(this::enrichDoctorDto)
                .collectList()
                .map(doctors -> {
                    int start = pageRequest.getPage() * pageRequest.getSize();
                    int end = Math.min(start + pageRequest.getSize(), doctors.size());
                    
                    List<DoctorDto> pageContent = doctors.subList(
                            Math.min(start, doctors.size()),
                            end
                    );
                    
                    return PageResponse.<DoctorDto>builder()
                            .content(pageContent)
                            .page(pageRequest.getPage())
                            .size(pageRequest.getSize())
                            .totalElements(doctors.size())
                            .totalPages((int) Math.ceil((double) doctors.size() / pageRequest.getSize()))
                            .first(pageRequest.getPage() == 0)
                            .last(end >= doctors.size())
                            .build();
                });
    }
    
    /**
     * Toggles doctor accepting patients status.
     */
    @Transactional
    public Mono<DoctorDto> toggleAcceptingPatients(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .switchIfEmpty(Mono.error(new NotFoundException("Doctor not found")))
                .flatMap(doctor -> {
                    doctor.setIsAcceptingPatients(!doctor.getIsAcceptingPatients());
                    return doctorRepository.save(doctor);
                })
                .flatMap(doctor -> 
                    eventPublisher.publishDoctorAvailabilityChanged(doctor)
                            .then(enrichDoctorDto(doctor))
                );
    }
    
    /**
     * Verifies a doctor (admin only).
     */
    @Transactional
    public Mono<DoctorDto> verifyDoctor(UUID doctorId) {
        log.info("Verifying doctor: {}", doctorId);
        
        return doctorRepository.findById(doctorId)
                .switchIfEmpty(Mono.error(new NotFoundException("Doctor not found")))
                .flatMap(doctor -> {
                    doctor.setIsVerified(true);
                    return doctorRepository.save(doctor);
                })
                .flatMap(doctor -> 
                    eventPublisher.publishDoctorVerified(doctor)
                            .then(enrichDoctorDto(doctor))
                )
                .doOnSuccess(dto -> log.info("Doctor verified: {}", dto.getId()));
    }
    
    // Helper methods
    
    private Mono<DoctorDto> enrichDoctorDto(Doctor doctor) {
        DoctorDto dto = doctorMapper.toDto(doctor);
        
        Mono<List<SpecializationDto>> specs = specializationRepository.findByDoctorId(doctor.getId())
                .map(doctorMapper::toDto)
                .collectList();
        
        Mono<List<QualificationDto>> quals = qualificationRepository.findByDoctorIdOrderByYearOfCompletionDesc(doctor.getId())
                .map(doctorMapper::toDto)
                .collectList();
        
        Mono<List<LanguageDto>> langs = languageRepository.findByDoctorId(doctor.getId())
                .map(doctorMapper::toDto)
                .collectList();
        
        Mono<List<ClinicDto>> clinics = clinicRepository.findByDoctorId(doctor.getId())
                .map(doctorMapper::toDto)
                .collectList();
        
        return Mono.zip(specs, quals, langs, clinics)
                .map(tuple -> {
                    dto.setSpecializations(tuple.getT1());
                    dto.setQualifications(tuple.getT2());
                    dto.setLanguages(tuple.getT3());
                    dto.setClinics(tuple.getT4());
                    return dto;
                });
    }
    
    private Mono<Void> linkSpecializations(UUID doctorId, List<UUID> specializationIds, UUID primaryId) {
        if (specializationIds == null || specializationIds.isEmpty()) {
            return Mono.empty();
        }
        
        return Flux.fromIterable(specializationIds)
                .flatMap(specId -> {
                    boolean isPrimary = specId.equals(primaryId);
                    return entityTemplate.getDatabaseClient()
                            .sql("INSERT INTO doctor_specializations (doctor_id, specialization_id, is_primary) VALUES (:doctorId, :specId, :isPrimary)")
                            .bind("doctorId", doctorId)
                            .bind("specId", specId)
                            .bind("isPrimary", isPrimary)
                            .fetch()
                            .rowsUpdated();
                })
                .then();
    }
    
    private Mono<Void> linkLanguages(UUID doctorId, List<UUID> languageIds) {
        if (languageIds == null || languageIds.isEmpty()) {
            return Mono.empty();
        }
        
        return Flux.fromIterable(languageIds)
                .flatMap(langId -> 
                    entityTemplate.getDatabaseClient()
                            .sql("INSERT INTO doctor_languages (doctor_id, language_id) VALUES (:doctorId, :langId)")
                            .bind("doctorId", doctorId)
                            .bind("langId", langId)
                            .fetch()
                            .rowsUpdated()
                )
                .then();
    }
    
    private Mono<Void> incrementProfileViews(UUID doctorId) {
        return entityTemplate.getDatabaseClient()
                .sql("UPDATE doctors SET profile_views = profile_views + 1 WHERE id = :id")
                .bind("id", doctorId)
                .fetch()
                .rowsUpdated()
                .then();
    }
}
