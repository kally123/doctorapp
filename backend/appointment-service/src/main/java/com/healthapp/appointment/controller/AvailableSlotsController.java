package com.healthapp.appointment.controller;

import com.healthapp.appointment.domain.ConsultationType;
import com.healthapp.appointment.dto.AvailableSlotsResponse;
import com.healthapp.appointment.service.AvailableSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors/{doctorId}/slots")
@RequiredArgsConstructor
public class AvailableSlotsController {
    
    private final AvailableSlotService slotService;
    
    @GetMapping
    public Mono<AvailableSlotsResponse> getAvailableSlots(
            @PathVariable String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) ConsultationType consultationType,
            @RequestParam(required = false) String clinicId) {
        
        if (endDate == null) {
            endDate = startDate.plusDays(7);
        }
        
        return slotService.getAvailableSlots(
                UUID.fromString(doctorId),
                startDate,
                endDate,
                consultationType,
                clinicId != null ? UUID.fromString(clinicId) : null
        );
    }
}
