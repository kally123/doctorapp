package com.healthapp.appointment.service;

import com.healthapp.appointment.domain.AvailableSlot;
import com.healthapp.appointment.domain.ConsultationType;
import com.healthapp.appointment.domain.SlotStatus;
import com.healthapp.appointment.dto.*;
import com.healthapp.appointment.repository.AvailableSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailableSlotService {
    
    private final AvailableSlotRepository slotRepo;
    
    public Mono<AvailableSlotsResponse> getAvailableSlots(
            UUID doctorId,
            LocalDate startDate,
            LocalDate endDate,
            ConsultationType consultationType,
            UUID clinicId) {
        
        var slotsFlux = consultationType != null
                ? slotRepo.findByDoctorIdAndDateRangeAndType(doctorId, startDate, endDate, consultationType)
                : slotRepo.findByDoctorIdAndDateRange(doctorId, startDate, endDate);
        
        if (clinicId != null) {
            slotsFlux = slotsFlux.filter(s -> clinicId.equals(s.getClinicId()));
        }
        
        return slotsFlux
                .collectList()
                .map(slotList -> buildResponse(doctorId, startDate, endDate, slotList));
    }
    
    private AvailableSlotsResponse buildResponse(
            UUID doctorId,
            LocalDate startDate,
            LocalDate endDate,
            List<AvailableSlot> slots) {
        
        Map<LocalDate, List<SlotDto>> groupedSlots = slots.stream()
                .map(this::toSlotDto)
                .collect(Collectors.groupingBy(SlotDto::getDate));
        
        List<DaySlots> days = groupedSlots.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> DaySlots.of(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        
        return AvailableSlotsResponse.builder()
                .doctorId(doctorId)
                .startDate(startDate)
                .endDate(endDate)
                .days(days)
                .totalAvailableSlots(slots.size())
                .build();
    }
    
    private SlotDto toSlotDto(AvailableSlot slot) {
        return SlotDto.builder()
                .slotId(slot.getId())
                .date(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .durationMinutes(slot.getSlotDurationMinutes())
                .consultationType(slot.getConsultationType())
                .clinicId(slot.getClinicId())
                .build();
    }
}
