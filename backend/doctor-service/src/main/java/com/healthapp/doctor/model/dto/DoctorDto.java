package com.healthapp.doctor.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO for doctor profile information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDto {
    String id;
    String userId;
    String fullName;
    String profilePictureUrl;
    String registrationNumber;
    String registrationCouncil;
    Integer experienceYears;
    String bio;
    BigDecimal consultationFee;
    BigDecimal videoConsultationFee;
    BigDecimal followupFee;
    BigDecimal rating;
    Integer reviewCount;
    Boolean isVerified;
    Boolean isAcceptingPatients;
    Integer profileCompleteness;
    List<SpecializationDto> specializations;
    List<QualificationDto> qualifications;
    List<LanguageDto> languages;
    List<ClinicDto> clinics;
    Instant createdAt;
    Instant updatedAt;
}
