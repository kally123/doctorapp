package com.healthapp.appointment.service;

import com.healthapp.appointment.domain.*;
import com.healthapp.appointment.dto.*;
import com.healthapp.appointment.repository.AvailableSlotRepository;
import com.healthapp.appointment.repository.BlockedSlotRepository;
import com.healthapp.appointment.repository.WeeklyAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {
    
    private final WeeklyAvailabilityRepository weeklyRepo;
    private final AvailableSlotRepository slotRepo;
    private final BlockedSlotRepository blockedRepo;
    
    @Value("${appointment.slots.generation-days-ahead:30}")
    private int slotGenerationDaysAhead;
    
    private static final String[] DAY_NAMES = {
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };
    
    public Flux<WeeklyAvailabilityDto> getWeeklySchedule(UUID doctorId) {
        return weeklyRepo.findActiveByDoctorId(doctorId)
                .map(this::toDto);
    }
    
    public Mono<WeeklyAvailabilityDto> addWeeklySlot(UUID doctorId, WeeklyAvailabilityRequest request) {
        return validateSlotRequest(request)
                .then(Mono.defer(() -> {
                    WeeklyAvailability slot = WeeklyAvailability.builder()
                            .doctorId(doctorId)
                            .clinicId(request.getClinicId() != null ? UUID.fromString(request.getClinicId()) : null)
                            .dayOfWeek(request.getDayOfWeek())
                            .startTime(request.getStartTime())
                            .endTime(request.getEndTime())
                            .slotDurationMinutes(request.getSlotDurationMinutes())
                            .bufferMinutes(request.getBufferMinutes() != null ? request.getBufferMinutes() : 5)
                            .consultationType(request.getConsultationType())
                            .maxPatientsPerSlot(1)
                            .isActive(true)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    
                    return weeklyRepo.save(slot);
                }))
                .doOnSuccess(slot -> regenerateSlots(doctorId).subscribe())
                .map(this::toDto);
    }
    
    public Mono<WeeklyAvailabilityDto> updateWeeklySlot(UUID doctorId, UUID slotId, WeeklyAvailabilityRequest request) {
        return weeklyRepo.findById(slotId)
                .filter(slot -> slot.getDoctorId().equals(doctorId))
                .switchIfEmpty(Mono.error(new RuntimeException("Slot not found or not authorized")))
                .flatMap(existing -> {
                    WeeklyAvailability updated = existing.toBuilder()
                            .dayOfWeek(request.getDayOfWeek())
                            .startTime(request.getStartTime())
                            .endTime(request.getEndTime())
                            .slotDurationMinutes(request.getSlotDurationMinutes())
                            .bufferMinutes(request.getBufferMinutes() != null ? request.getBufferMinutes() : 5)
                            .consultationType(request.getConsultationType())
                            .updatedAt(Instant.now())
                            .build();
                    
                    return weeklyRepo.save(updated);
                })
                .doOnSuccess(slot -> regenerateSlots(doctorId).subscribe())
                .map(this::toDto);
    }
    
    public Mono<Void> deleteWeeklySlot(UUID doctorId, UUID slotId) {
        return weeklyRepo.findById(slotId)
                .filter(slot -> slot.getDoctorId().equals(doctorId))
                .flatMap(slot -> {
                    slot.setIsActive(false);
                    slot.setUpdatedAt(Instant.now());
                    return weeklyRepo.save(slot);
                })
                .doOnSuccess(slot -> regenerateSlots(doctorId).subscribe())
                .then();
    }
    
    public Flux<WeeklyAvailabilityDto> setWeeklySchedule(UUID doctorId, List<WeeklyAvailabilityRequest> schedule) {
        return weeklyRepo.findByDoctorId(doctorId)
                .map(slot -> {
                    slot.setIsActive(false);
                    return slot;
                })
                .flatMap(weeklyRepo::save)
                .thenMany(Flux.fromIterable(schedule)
                        .flatMap(request -> addWeeklySlot(doctorId, request)));
    }
    
    public Mono<Void> regenerateSlots(UUID doctorId) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(slotGenerationDaysAhead);
        
        return weeklyRepo.findByDoctorIdAndIsActive(doctorId, true)
                .collectList()
                .flatMap(weeklySlots -> generateSlotsForDateRange(doctorId, weeklySlots, startDate, endDate));
    }
    
    private Mono<Void> generateSlotsForDateRange(
            UUID doctorId,
            List<WeeklyAvailability> weeklySlots,
            LocalDate startDate,
            LocalDate endDate) {
        
        List<AvailableSlot> slotsToCreate = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            int dayOfWeek = date.getDayOfWeek().getValue() % 7; // Convert to 0-6 (Sun-Sat)
            
            for (WeeklyAvailability weekly : weeklySlots) {
                if (weekly.getDayOfWeek().equals(dayOfWeek)) {
                    LocalTime current = weekly.getStartTime();
                    while (current.plusMinutes(weekly.getSlotDurationMinutes()).isBefore(weekly.getEndTime()) ||
                           current.plusMinutes(weekly.getSlotDurationMinutes()).equals(weekly.getEndTime())) {
                        
                        AvailableSlot slot = AvailableSlot.builder()
                                .doctorId(doctorId)
                                .clinicId(weekly.getClinicId())
                                .slotDate(date)
                                .startTime(current)
                                .endTime(current.plusMinutes(weekly.getSlotDurationMinutes()))
                                .consultationType(weekly.getConsultationType())
                                .slotDurationMinutes(weekly.getSlotDurationMinutes())
                                .status(SlotStatus.AVAILABLE)
                                .createdAt(Instant.now())
                                .build();
                        
                        slotsToCreate.add(slot);
                        
                        current = current.plusMinutes(
                                weekly.getSlotDurationMinutes() + weekly.getBufferMinutes());
                    }
                }
            }
        }
        
        // Delete existing future available slots and insert new ones
        return slotRepo.deleteByDoctorIdAndSlotDateAfterAndStatus(doctorId, startDate.minusDays(1))
                .then(Flux.fromIterable(slotsToCreate)
                        .flatMap(slotRepo::save)
                        .then());
    }
    
    public Mono<BlockedSlot> blockSlot(UUID doctorId, BlockSlotRequest request) {
        BlockedSlot block = BlockedSlot.builder()
                .doctorId(doctorId)
                .startDatetime(request.getStartDatetime())
                .endDatetime(request.getEndDatetime())
                .reason(request.getReason())
                .blockType(request.getBlockType() != null ? request.getBlockType() : "LEAVE")
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .recurrencePattern(request.getRecurrencePattern())
                .createdAt(Instant.now())
                .build();
        
        return blockedRepo.save(block)
                .doOnSuccess(b -> regenerateSlots(doctorId).subscribe());
    }
    
    public Mono<Void> unblockSlot(UUID doctorId, UUID blockId) {
        return blockedRepo.findById(blockId)
                .filter(block -> block.getDoctorId().equals(doctorId))
                .flatMap(blockedRepo::delete)
                .doOnSuccess(v -> regenerateSlots(doctorId).subscribe());
    }
    
    public Flux<BlockedSlot> getBlockedSlots(UUID doctorId) {
        return blockedRepo.findByDoctorId(doctorId);
    }
    
    private Mono<Void> validateSlotRequest(WeeklyAvailabilityRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime()) || 
            request.getStartTime().equals(request.getEndTime())) {
            return Mono.error(new IllegalArgumentException("Start time must be before end time"));
        }
        
        if (request.getConsultationType() == ConsultationType.IN_PERSON && 
            request.getClinicId() == null) {
            return Mono.error(new IllegalArgumentException("Clinic ID is required for in-person consultations"));
        }
        
        return Mono.empty();
    }
    
    private WeeklyAvailabilityDto toDto(WeeklyAvailability slot) {
        return WeeklyAvailabilityDto.builder()
                .id(slot.getId())
                .doctorId(slot.getDoctorId())
                .clinicId(slot.getClinicId())
                .dayOfWeek(slot.getDayOfWeek())
                .dayName(DAY_NAMES[slot.getDayOfWeek()])
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .slotDurationMinutes(slot.getSlotDurationMinutes())
                .bufferMinutes(slot.getBufferMinutes())
                .consultationType(slot.getConsultationType())
                .maxPatientsPerSlot(slot.getMaxPatientsPerSlot())
                .isActive(slot.getIsActive())
                .build();
    }
}
