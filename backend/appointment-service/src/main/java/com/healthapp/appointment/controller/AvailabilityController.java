package com.healthapp.appointment.controller;

import com.healthapp.appointment.domain.BlockedSlot;
import com.healthapp.appointment.dto.*;
import com.healthapp.appointment.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors/me/availability")
@RequiredArgsConstructor
public class AvailabilityController {
    
    private final AvailabilityService availabilityService;
    
    @GetMapping("/weekly")
    public Flux<WeeklyAvailabilityDto> getWeeklySchedule(
            @RequestHeader("X-User-Id") String userId) {
        return availabilityService.getWeeklySchedule(UUID.fromString(userId));
    }
    
    @PostMapping("/weekly")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<WeeklyAvailabilityDto> addWeeklySlot(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody WeeklyAvailabilityRequest request) {
        return availabilityService.addWeeklySlot(UUID.fromString(userId), request);
    }
    
    @PutMapping("/weekly/{slotId}")
    public Mono<WeeklyAvailabilityDto> updateWeeklySlot(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String slotId,
            @Valid @RequestBody WeeklyAvailabilityRequest request) {
        return availabilityService.updateWeeklySlot(
                UUID.fromString(userId), UUID.fromString(slotId), request);
    }
    
    @DeleteMapping("/weekly/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteWeeklySlot(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String slotId) {
        return availabilityService.deleteWeeklySlot(
                UUID.fromString(userId), UUID.fromString(slotId));
    }
    
    @PostMapping("/weekly/bulk")
    public Flux<WeeklyAvailabilityDto> setWeeklySchedule(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody List<WeeklyAvailabilityRequest> schedule) {
        return availabilityService.setWeeklySchedule(UUID.fromString(userId), schedule);
    }
    
    @PostMapping("/block")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BlockedSlot> blockSlot(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody BlockSlotRequest request) {
        return availabilityService.blockSlot(UUID.fromString(userId), request);
    }
    
    @DeleteMapping("/block/{blockId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> unblockSlot(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String blockId) {
        return availabilityService.unblockSlot(
                UUID.fromString(userId), UUID.fromString(blockId));
    }
    
    @GetMapping("/blocks")
    public Flux<BlockedSlot> getBlockedSlots(
            @RequestHeader("X-User-Id") String userId) {
        return availabilityService.getBlockedSlots(UUID.fromString(userId));
    }
    
    @PostMapping("/regenerate")
    public Mono<Void> regenerateSlots(
            @RequestHeader("X-User-Id") String userId) {
        return availabilityService.regenerateSlots(UUID.fromString(userId));
    }
}
