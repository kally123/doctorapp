package com.healthapp.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DaySlots {
    private LocalDate date;
    private String dayName;
    private List<SlotDto> slots;
    
    public static DaySlots of(LocalDate date, List<SlotDto> slots) {
        return DaySlots.builder()
                .date(date)
                .dayName(date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                .slots(slots.stream()
                        .sorted(Comparator.comparing(SlotDto::getStartTime))
                        .collect(Collectors.toList()))
                .build();
    }
}
