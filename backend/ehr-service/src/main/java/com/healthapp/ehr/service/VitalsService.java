package com.healthapp.ehr.service;

import com.healthapp.ehr.domain.VitalReading;
import com.healthapp.ehr.domain.enums.VitalType;
import com.healthapp.ehr.dto.RecordVitalRequest;
import com.healthapp.ehr.dto.VitalResponse;
import com.healthapp.ehr.dto.VitalStatisticsResponse;
import com.healthapp.ehr.event.EhrEventPublisher;
import com.healthapp.ehr.repository.VitalReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing vital signs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VitalsService {

    private final VitalReadingRepository vitalRepository;
    private final EhrEventPublisher eventPublisher;

    // Normal ranges for vitals (simplified)
    private static final Map<VitalType, double[]> NORMAL_RANGES = Map.of(
            VitalType.BLOOD_PRESSURE_SYSTOLIC, new double[]{90, 140},
            VitalType.BLOOD_PRESSURE_DIASTOLIC, new double[]{60, 90},
            VitalType.HEART_RATE, new double[]{60, 100},
            VitalType.RESPIRATORY_RATE, new double[]{12, 20},
            VitalType.TEMPERATURE, new double[]{36.1, 37.2},
            VitalType.OXYGEN_SATURATION, new double[]{95, 100},
            VitalType.BLOOD_GLUCOSE_FASTING, new double[]{70, 100},
            VitalType.BLOOD_GLUCOSE_POSTPRANDIAL, new double[]{70, 140}
    );

    private static final Map<VitalType, String> UNITS = Map.of(
            VitalType.BLOOD_PRESSURE_SYSTOLIC, "mmHg",
            VitalType.BLOOD_PRESSURE_DIASTOLIC, "mmHg",
            VitalType.HEART_RATE, "bpm",
            VitalType.RESPIRATORY_RATE, "breaths/min",
            VitalType.TEMPERATURE, "Â°C",
            VitalType.OXYGEN_SATURATION, "%",
            VitalType.WEIGHT, "kg",
            VitalType.HEIGHT, "cm",
            VitalType.BLOOD_GLUCOSE, "mg/dL"
    );

    /**
     * Record a vital reading.
     */
    public Mono<VitalResponse> recordVital(RecordVitalRequest request) {
        log.info("Recording vital for patient: {}, type: {}, value: {}", 
                request.getPatientId(), request.getVitalType(), request.getValue());

        boolean isAbnormal = checkIfAbnormal(request.getVitalType(), request.getValue());
        String abnormalReason = isAbnormal ? getAbnormalReason(request.getVitalType(), request.getValue()) : null;

        VitalReading reading = VitalReading.builder()
                .patientId(request.getPatientId())
                .vitalType(request.getVitalType())
                .value(request.getValue())
                .unit(request.getUnit() != null ? request.getUnit() : UNITS.get(request.getVitalType()))
                .secondaryValue(request.getSecondaryValue())
                .notes(request.getNotes())
                .source(request.getSource() != null ? request.getSource() : "MANUAL")
                .deviceId(request.getDeviceId())
                .deviceName(request.getDeviceName())
                .consultationId(request.getConsultationId())
                .recordedById(request.getRecordedById())
                .recordedByName(request.getRecordedByName())
                .recordedByRole(request.getRecordedByRole())
                .isAbnormal(isAbnormal)
                .abnormalReason(abnormalReason)
                .recordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now())
                .build();

        return vitalRepository.save(reading)
                .doOnSuccess(saved -> eventPublisher.publishVitalRecorded(
                        saved.getPatientId(), saved.getId(), saved.getVitalType().name(), 
                        saved.getValue(), saved.getRecordedById()))
                .map(this::toResponse);
    }

    /**
     * Record blood pressure (both systolic and diastolic).
     */
    public Flux<VitalResponse> recordBloodPressure(
            UUID patientId, Double systolic, Double diastolic, 
            String notes, UUID recordedById, String recordedByName) {

        RecordVitalRequest systolicRequest = RecordVitalRequest.builder()
                .patientId(patientId)
                .vitalType(VitalType.BLOOD_PRESSURE_SYSTOLIC)
                .value(systolic)
                .secondaryValue(diastolic)
                .notes(notes)
                .recordedById(recordedById)
                .recordedByName(recordedByName)
                .build();

        RecordVitalRequest diastolicRequest = RecordVitalRequest.builder()
                .patientId(patientId)
                .vitalType(VitalType.BLOOD_PRESSURE_DIASTOLIC)
                .value(diastolic)
                .notes(notes)
                .recordedById(recordedById)
                .recordedByName(recordedByName)
                .build();

        return Flux.merge(recordVital(systolicRequest), recordVital(diastolicRequest));
    }

    /**
     * Get all vitals for a patient.
     */
    public Flux<VitalResponse> getPatientVitals(UUID patientId) {
        return vitalRepository.findByPatientIdOrderByRecordedAtDesc(patientId)
                .map(this::toResponse);
    }

    /**
     * Get vitals by type for a patient.
     */
    public Flux<VitalResponse> getPatientVitalsByType(UUID patientId, VitalType vitalType) {
        return vitalRepository.findByPatientIdAndVitalTypeOrderByRecordedAtDesc(patientId, vitalType)
                .map(this::toResponse);
    }

    /**
     * Get vitals in a time range.
     */
    public Flux<VitalResponse> getPatientVitalsInRange(
            UUID patientId, VitalType vitalType, LocalDateTime start, LocalDateTime end) {
        return vitalRepository.findByPatientIdAndVitalTypeAndRecordedAtBetweenOrderByRecordedAtDesc(
                        patientId, vitalType, start, end)
                .map(this::toResponse);
    }

    /**
     * Get latest vital of each type for a patient.
     */
    public Flux<VitalResponse> getLatestVitals(UUID patientId) {
        return Flux.fromArray(VitalType.values())
                .flatMap(type -> vitalRepository.findFirstByPatientIdAndVitalTypeOrderByRecordedAtDesc(patientId, type))
                .map(this::toResponse);
    }

    /**
     * Get abnormal vitals for a patient.
     */
    public Flux<VitalResponse> getAbnormalVitals(UUID patientId) {
        return vitalRepository.findByPatientIdAndIsAbnormalTrueOrderByRecordedAtDesc(patientId)
                .map(this::toResponse);
    }

    /**
     * Get vital statistics for a patient.
     */
    public Mono<VitalStatisticsResponse> getVitalStatistics(UUID patientId, VitalType vitalType) {
        return vitalRepository.getVitalStatistics(patientId, vitalType)
                .zipWith(vitalRepository.findFirstByPatientIdAndVitalTypeOrderByRecordedAtDesc(patientId, vitalType))
                .map(tuple -> {
                    var stats = tuple.getT1();
                    var latest = tuple.getT2();
                    return VitalStatisticsResponse.builder()
                            .patientId(patientId)
                            .vitalType(vitalType)
                            .unit(UNITS.get(vitalType))
                            .average(stats.getAvg())
                            .minimum(stats.getMin())
                            .maximum(stats.getMax())
                            .count(stats.getCount())
                            .latestValue(latest.getValue())
                            .latestRecordedAt(latest.getRecordedAt())
                            .build();
                })
                .defaultIfEmpty(VitalStatisticsResponse.builder()
                        .patientId(patientId)
                        .vitalType(vitalType)
                        .unit(UNITS.get(vitalType))
                        .count(0L)
                        .build());
    }

    /**
     * Get vital statistics in a time period.
     */
    public Mono<VitalStatisticsResponse> getVitalStatisticsInPeriod(
            UUID patientId, VitalType vitalType, LocalDateTime start, LocalDateTime end) {
        return vitalRepository.getVitalStatisticsInPeriod(patientId, vitalType, start, end)
                .map(stats -> VitalStatisticsResponse.builder()
                        .patientId(patientId)
                        .vitalType(vitalType)
                        .unit(UNITS.get(vitalType))
                        .average(stats.getAvg())
                        .minimum(stats.getMin())
                        .maximum(stats.getMax())
                        .count(stats.getCount())
                        .periodStart(start)
                        .periodEnd(end)
                        .build())
                .defaultIfEmpty(VitalStatisticsResponse.builder()
                        .patientId(patientId)
                        .vitalType(vitalType)
                        .unit(UNITS.get(vitalType))
                        .count(0L)
                        .periodStart(start)
                        .periodEnd(end)
                        .build());
    }

    private boolean checkIfAbnormal(VitalType type, Double value) {
        if (!NORMAL_RANGES.containsKey(type) || value == null) {
            return false;
        }
        double[] range = NORMAL_RANGES.get(type);
        return value < range[0] || value > range[1];
    }

    private String getAbnormalReason(VitalType type, Double value) {
        if (!NORMAL_RANGES.containsKey(type) || value == null) {
            return null;
        }
        double[] range = NORMAL_RANGES.get(type);
        if (value < range[0]) {
            return String.format("Below normal range (%.1f-%.1f)", range[0], range[1]);
        } else if (value > range[1]) {
            return String.format("Above normal range (%.1f-%.1f)", range[0], range[1]);
        }
        return null;
    }

    private VitalResponse toResponse(VitalReading reading) {
        return VitalResponse.builder()
                .id(reading.getId())
                .patientId(reading.getPatientId())
                .vitalType(reading.getVitalType())
                .value(reading.getValue())
                .unit(reading.getUnit())
                .secondaryValue(reading.getSecondaryValue())
                .notes(reading.getNotes())
                .source(reading.getSource())
                .deviceId(reading.getDeviceId())
                .deviceName(reading.getDeviceName())
                .consultationId(reading.getConsultationId())
                .recordedById(reading.getRecordedById())
                .recordedByName(reading.getRecordedByName())
                .recordedByRole(reading.getRecordedByRole())
                .isAbnormal(reading.getIsAbnormal())
                .abnormalReason(reading.getAbnormalReason())
                .recordedAt(reading.getRecordedAt())
                .createdAt(reading.getCreatedAt())
                .build();
    }
}
