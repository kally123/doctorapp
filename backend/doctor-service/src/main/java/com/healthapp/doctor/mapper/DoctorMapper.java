package com.healthapp.doctor.mapper;

import com.healthapp.doctor.model.dto.*;
import com.healthapp.doctor.model.entity.*;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Doctor-related entities and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DoctorMapper {
    
    // Doctor mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isVerified", constant = "false")
    @Mapping(target = "isAcceptingPatients", constant = "true")
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "reviewCount", constant = "0")
    Doctor toEntity(CreateDoctorRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    Doctor updateEntity(@MappingTarget Doctor doctor, UpdateDoctorRequest request);
    
    @Mapping(target = "specializations", ignore = true)
    @Mapping(target = "qualifications", ignore = true)
    @Mapping(target = "languages", ignore = true)
    @Mapping(target = "clinics", ignore = true)
    DoctorDto toDto(Doctor doctor);
    
    // Specialization mappings
    SpecializationDto toDto(Specialization specialization);
    List<SpecializationDto> toSpecializationDtoList(List<Specialization> specializations);
    
    // Qualification mappings
    QualificationDto toDto(DoctorQualification qualification);
    List<QualificationDto> toQualificationDtoList(List<DoctorQualification> qualifications);
    
    // Language mappings
    LanguageDto toDto(Language language);
    List<LanguageDto> toLanguageDtoList(List<Language> languages);
    
    // Clinic mappings
    @Mapping(target = "isPrimary", ignore = true)
    ClinicDto toDto(Clinic clinic);
    List<ClinicDto> toClinicDtoList(List<Clinic> clinics);
}
