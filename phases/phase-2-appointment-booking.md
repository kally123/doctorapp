# Phase 2: Appointment Booking - Detailed Implementation Plan

## Phase Overview

| Attribute | Details |
|-----------|---------|
| **Duration** | 4 Weeks |
| **Start Date** | _Phase 1 End Date + 1 day_ |
| **End Date** | _Start Date + 4 weeks_ |
| **Team Size** | 10-12 members |
| **Goal** | Complete appointment booking with payments and notifications |

---

## Phase 2 Objectives

1. ✅ Implement doctor availability management
2. ✅ Build complete appointment booking flow
3. ✅ Integrate payment gateway for appointment fees
4. ✅ Implement Saga pattern for reliable booking transactions
5. ✅ Build notification service (Email, SMS, Push)
6. ✅ Create appointment management UI for patients and doctors
7. ✅ Implement appointment reminders

---

## Prerequisites from Phase 1

Before starting Phase 2, ensure the following are complete:

| Prerequisite | Status |
|--------------|--------|
| User Service deployed and functional | ⬜ |
| Doctor Service deployed and functional | ⬜ |
| API Gateway with JWT validation | ⬜ |
| Kafka cluster with event publishing | ⬜ |
| Patient Web App with auth and doctor search | ⬜ |
| Doctor Dashboard with profile management | ⬜ |

---

## Team Allocation for Phase 2

| Role | Name | Focus Area |
|------|------|------------|
| Tech Lead | _TBD_ | Architecture, Saga pattern, code reviews |
| Backend 1 | _TBD_ | Appointment Service |
| Backend 2 | _TBD_ | Doctor Availability (Doctor Service enhancement) |
| Backend 3 | _TBD_ | Payment Service |
| Backend 4 | _TBD_ | Notification Service |
| Frontend 1 | _TBD_ | Patient Web App - Booking flow |
| Frontend 2 | _TBD_ | Doctor Dashboard - Calendar & Appointments |
| DevOps | _TBD_ | Infrastructure, Payment gateway setup |
| QA 1 | _TBD_ | Testing - Booking flows |
| QA 2 | _TBD_ | Testing - Payment flows |

---

## Sprint Breakdown

### Sprint 4 (Week 7-8): Appointment Service & Availability

**Sprint Goal**: Doctors can configure availability; Patients can view available slots and initiate bookings.

---

#### Doctor Availability Tasks - Sprint 4

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B7.1 | Availability Schema | Design availability_slots, blocked_slots tables | Backend 2 | 8 | P0 | Migrations run successfully |
| B7.2 | Availability Entity & Repository | R2DBC models for availability | Backend 2 | 8 | P0 | Repository tests pass |
| B7.3 | Weekly Schedule API | CRUD for recurring weekly slots | Backend 2 | 12 | P0 | Can set weekly schedule |
| B7.4 | Override Slots API | One-time availability changes | Backend 2 | 8 | P0 | Can override specific days |
| B7.5 | Block Slots API | Mark slots as unavailable (leave, holiday) | Backend 2 | 8 | P0 | Can block time ranges |
| B7.6 | Get Available Slots | Return available slots for date range | Backend 2 | 12 | P0 | Returns correct slots |
| B7.7 | Slot Duration Config | Support different consultation durations | Backend 2 | 4 | P0 | Duration configurable |
| B7.8 | Buffer Time | Add buffer between appointments | Backend 2 | 4 | P1 | Buffer time applied |
| B7.9 | Sync to Search | Publish availability events to Kafka | Backend 2 | 4 | P1 | Search shows availability |
| B7.10 | Unit & Integration Tests | 80%+ coverage | Backend 2 | 12 | P0 | Tests pass |

**Availability Schema:**

<details>
<summary><strong>B7.1 - Availability Database Schema</strong></summary>

```sql
-- V2__create_availability_tables.sql

-- Consultation types
CREATE TYPE consultation_type AS ENUM ('IN_PERSON', 'VIDEO', 'AUDIO', 'CHAT');

-- Weekly recurring availability slots
CREATE TABLE weekly_availability (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID NOT NULL,  -- References doctors table
    clinic_id UUID,           -- NULL for video consultations
    
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),  -- 0=Sunday
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    
    slot_duration_minutes INT NOT NULL DEFAULT 15,
    buffer_minutes INT DEFAULT 5,  -- Gap between appointments
    
    consultation_type consultation_type NOT NULL DEFAULT 'IN_PERSON',
    max_patients_per_slot INT DEFAULT 1,  -- For group consultations
    
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT valid_time_range CHECK (start_time < end_time),
    CONSTRAINT unique_doctor_slot UNIQUE (doctor_id, day_of_week, start_time, clinic_id)
);

-- Date-specific overrides (extend or reduce hours for specific dates)
CREATE TABLE availability_overrides (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID NOT NULL,
    clinic_id UUID,
    
    override_date DATE NOT NULL,
    start_time TIME,           -- NULL means closed for the day
    end_time TIME,
    
    slot_duration_minutes INT,
    consultation_type consultation_type,
    
    reason VARCHAR(255),       -- "Extended hours", "Reduced hours"
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_date_override UNIQUE (doctor_id, override_date, clinic_id)
);

-- Blocked time ranges (leave, unavailable periods)
CREATE TABLE blocked_slots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID NOT NULL,
    clinic_id UUID,            -- NULL = blocked for all locations
    
    start_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    end_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    
    block_type VARCHAR(50) DEFAULT 'LEAVE',  -- LEAVE, PERSONAL, EMERGENCY
    reason VARCHAR(255),
    
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_pattern JSONB,  -- {"frequency": "weekly", "until": "2026-12-31"}
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT valid_block_range CHECK (start_datetime < end_datetime)
);

-- Generated slots (materialized for performance)
CREATE TABLE available_slots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID NOT NULL,
    clinic_id UUID,
    
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    
    consultation_type consultation_type NOT NULL,
    slot_duration_minutes INT NOT NULL,
    
    status VARCHAR(20) DEFAULT 'AVAILABLE',  -- AVAILABLE, BOOKED, BLOCKED
    appointment_id UUID,       -- Set when booked
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_slot UNIQUE (doctor_id, slot_date, start_time, clinic_id)
);

-- Indexes
CREATE INDEX idx_weekly_availability_doctor ON weekly_availability(doctor_id) WHERE is_active = TRUE;
CREATE INDEX idx_weekly_availability_day ON weekly_availability(day_of_week) WHERE is_active = TRUE;
CREATE INDEX idx_availability_overrides_doctor_date ON availability_overrides(doctor_id, override_date);
CREATE INDEX idx_blocked_slots_doctor ON blocked_slots(doctor_id, start_datetime, end_datetime);
CREATE INDEX idx_available_slots_doctor_date ON available_slots(doctor_id, slot_date) WHERE status = 'AVAILABLE';
CREATE INDEX idx_available_slots_status ON available_slots(status, slot_date);
```
</details>

<details>
<summary><strong>B7.3 - Weekly Schedule API Implementation</strong></summary>

```java
// WeeklyAvailability.java
public record WeeklyAvailability(
    String id,
    String doctorId,
    String clinicId,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    Integer slotDurationMinutes,
    Integer bufferMinutes,
    ConsultationType consultationType,
    Integer maxPatientsPerSlot,
    Boolean isActive
) {}

// WeeklyAvailabilityRequest.java
@Value
@Builder
public class WeeklyAvailabilityRequest {
    @NotNull DayOfWeek dayOfWeek;
    @NotNull LocalTime startTime;
    @NotNull LocalTime endTime;
    @NotNull @Min(5) @Max(120) Integer slotDurationMinutes;
    @Min(0) @Max(60) Integer bufferMinutes;
    @NotNull ConsultationType consultationType;
    String clinicId;  // Required for IN_PERSON
}

// AvailabilityController.java
@RestController
@RequestMapping("/api/v1/doctors/me/availability")
@RequiredArgsConstructor
public class AvailabilityController {
    
    private final AvailabilityService availabilityService;
    
    @GetMapping("/weekly")
    public Flux<WeeklyAvailability> getWeeklySchedule(
            @RequestHeader("X-User-Id") String userId) {
        return availabilityService.getWeeklySchedule(userId);
    }
    
    @PostMapping("/weekly")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<WeeklyAvailability> addWeeklySlot(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody WeeklyAvailabilityRequest request) {
        return availabilityService.addWeeklySlot(userId, request);
    }
    
    @PutMapping("/weekly/{slotId}")
    public Mono<WeeklyAvailability> updateWeeklySlot(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String slotId,
            @Valid @RequestBody WeeklyAvailabilityRequest request) {
        return availabilityService.updateWeeklySlot(userId, slotId, request);
    }
    
    @DeleteMapping("/weekly/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteWeeklySlot(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String slotId) {
        return availabilityService.deleteWeeklySlot(userId, slotId);
    }
    
    @PostMapping("/weekly/bulk")
    public Flux<WeeklyAvailability> setWeeklySchedule(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody List<WeeklyAvailabilityRequest> schedule) {
        return availabilityService.setWeeklySchedule(userId, schedule);
    }
}

// AvailabilityService.java
@Service
@RequiredArgsConstructor
public class AvailabilityService {
    
    private final WeeklyAvailabilityRepository weeklyRepo;
    private final AvailableSlotRepository slotRepo;
    private final DoctorRepository doctorRepo;
    private final AvailabilityEventPublisher eventPublisher;
    
    public Mono<WeeklyAvailability> addWeeklySlot(String userId, WeeklyAvailabilityRequest request) {
        return doctorRepo.findByUserId(userId)
            .switchIfEmpty(Mono.error(new NotFoundException("Doctor not found")))
            .flatMap(doctor -> validateSlotRequest(doctor.getId(), request))
            .flatMap(doctor -> {
                WeeklyAvailability slot = WeeklyAvailability.builder()
                    .doctorId(doctor.getId())
                    .clinicId(request.getClinicId())
                    .dayOfWeek(request.getDayOfWeek())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .slotDurationMinutes(request.getSlotDurationMinutes())
                    .bufferMinutes(request.getBufferMinutes() != null ? request.getBufferMinutes() : 5)
                    .consultationType(request.getConsultationType())
                    .maxPatientsPerSlot(1)
                    .isActive(true)
                    .build();
                    
                return weeklyRepo.save(slot);
            })
            .doOnSuccess(slot -> regenerateSlots(slot.getDoctorId()))
            .doOnSuccess(slot -> eventPublisher.publishAvailabilityUpdated(slot.getDoctorId()));
    }
    
    /**
     * Regenerate available_slots for next N days based on weekly schedule
     */
    public Mono<Void> regenerateSlots(String doctorId) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);  // Generate 30 days ahead
        
        return weeklyRepo.findByDoctorIdAndIsActive(doctorId, true)
            .collectList()
            .flatMap(weeklySlots -> 
                generateSlotsForDateRange(doctorId, weeklySlots, startDate, endDate))
            .then();
    }
    
    private Mono<Void> generateSlotsForDateRange(
            String doctorId, 
            List<WeeklyAvailability> weeklySlots,
            LocalDate startDate,
            LocalDate endDate) {
        
        List<AvailableSlot> slotsToCreate = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            
            for (WeeklyAvailability weekly : weeklySlots) {
                if (weekly.getDayOfWeek().equals(dayOfWeek)) {
                    // Generate individual slots for this day
                    LocalTime current = weekly.getStartTime();
                    while (current.plusMinutes(weekly.getSlotDurationMinutes())
                            .isBefore(weekly.getEndTime()) ||
                           current.plusMinutes(weekly.getSlotDurationMinutes())
                            .equals(weekly.getEndTime())) {
                        
                        AvailableSlot slot = AvailableSlot.builder()
                            .doctorId(doctorId)
                            .clinicId(weekly.getClinicId())
                            .slotDate(date)
                            .startTime(current)
                            .endTime(current.plusMinutes(weekly.getSlotDurationMinutes()))
                            .consultationType(weekly.getConsultationType())
                            .slotDurationMinutes(weekly.getSlotDurationMinutes())
                            .status(SlotStatus.AVAILABLE)
                            .build();
                            
                        slotsToCreate.add(slot);
                        
                        // Move to next slot (include buffer)
                        current = current.plusMinutes(
                            weekly.getSlotDurationMinutes() + weekly.getBufferMinutes());
                    }
                }
            }
        }
        
        // Delete existing future slots and insert new ones
        return slotRepo.deleteByDoctorIdAndSlotDateAfterAndStatus(
                doctorId, startDate.minusDays(1), SlotStatus.AVAILABLE)
            .thenMany(slotRepo.saveAll(slotsToCreate))
            .then();
    }
}
```
</details>

<details>
<summary><strong>B7.6 - Get Available Slots API</strong></summary>

```java
// AvailableSlotsController.java
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
            endDate = startDate.plusDays(7);  // Default: 1 week
        }
        
        return slotService.getAvailableSlots(doctorId, startDate, endDate, consultationType, clinicId);
    }
}

// AvailableSlotService.java
@Service
@RequiredArgsConstructor
public class AvailableSlotService {
    
    private final AvailableSlotRepository slotRepo;
    private final BlockedSlotRepository blockedRepo;
    private final AppointmentRepository appointmentRepo;
    
    public Mono<AvailableSlotsResponse> getAvailableSlots(
            String doctorId,
            LocalDate startDate,
            LocalDate endDate,
            ConsultationType consultationType,
            String clinicId) {
        
        // Get all slots for date range
        Flux<AvailableSlot> slots = slotRepo.findByDoctorIdAndDateRange(
            doctorId, startDate, endDate, SlotStatus.AVAILABLE);
        
        // Apply filters
        if (consultationType != null) {
            slots = slots.filter(s -> s.getConsultationType().equals(consultationType));
        }
        if (clinicId != null) {
            slots = slots.filter(s -> clinicId.equals(s.getClinicId()));
        }
        
        // Group by date
        return slots.collectList()
            .map(slotList -> {
                Map<LocalDate, List<SlotDto>> groupedSlots = slotList.stream()
                    .map(this::toSlotDto)
                    .collect(Collectors.groupingBy(SlotDto::getDate));
                
                List<DaySlots> days = groupedSlots.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> new DaySlots(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
                
                return AvailableSlotsResponse.builder()
                    .doctorId(doctorId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .days(days)
                    .totalAvailableSlots(slotList.size())
                    .build();
            });
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

// Response DTOs
@Value
@Builder
public class AvailableSlotsResponse {
    String doctorId;
    LocalDate startDate;
    LocalDate endDate;
    List<DaySlots> days;
    int totalAvailableSlots;
}

@Value
public class DaySlots {
    LocalDate date;
    String dayName;  // "Monday", "Tuesday"
    List<SlotDto> slots;
    
    public DaySlots(LocalDate date, List<SlotDto> slots) {
        this.date = date;
        this.dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        this.slots = slots.stream()
            .sorted(Comparator.comparing(SlotDto::getStartTime))
            .collect(Collectors.toList());
    }
}

@Value
@Builder
public class SlotDto {
    String slotId;
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;
    int durationMinutes;
    ConsultationType consultationType;
    String clinicId;
}
```
</details>

---

#### Appointment Service Tasks - Sprint 4

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B6.1 | Create Appointment Service | Generate from template, configure | Backend 1 | 4 | P0 | Service runs |
| B6.2 | Appointments Schema | Design appointments table with all states | Backend 1 | 8 | P0 | Migrations run |
| B6.3 | Appointment Entity & Repository | R2DBC models with queries | Backend 1 | 8 | P0 | Repository tests pass |
| B6.4 | Reserve Slot Endpoint | POST /appointments/reserve (temporary hold) | Backend 1 | 12 | P0 | Slot reserved for 10 min |
| B6.5 | Confirm Appointment | Confirm after payment | Backend 1 | 8 | P0 | Appointment confirmed |
| B6.6 | Cancel Appointment | Cancel with reason, trigger refund | Backend 1 | 8 | P0 | Cancellation works |
| B6.7 | Reschedule Appointment | Move to different slot | Backend 1 | 8 | P1 | Reschedule works |
| B6.8 | Get Patient Appointments | List with filters and pagination | Backend 1 | 8 | P0 | Returns correct list |
| B6.9 | Get Doctor Appointments | Calendar view, daily list | Backend 1 | 8 | P0 | Returns correct list |
| B6.10 | Appointment Events | Publish all state changes to Kafka | Backend 1 | 8 | P0 | Events published |
| B6.11 | Slot Status Updates | Update available_slots on booking | Backend 1 | 4 | P0 | Slots marked as booked |
| B6.12 | Expiry Handler | Release expired reservations | Backend 1 | 8 | P0 | Expired slots released |
| B6.13 | Unit & Integration Tests | 80%+ coverage | Backend 1 | 12 | P0 | Tests pass |

**Appointment Service Schema:**

<details>
<summary><strong>B6.2 - Appointments Database Schema</strong></summary>

```sql
-- V1__create_appointments_table.sql

-- Appointment status enum
CREATE TYPE appointment_status AS ENUM (
    'PENDING_PAYMENT',     -- Slot reserved, waiting for payment
    'PAYMENT_FAILED',      -- Payment attempt failed
    'CONFIRMED',           -- Payment successful, appointment confirmed
    'REMINDER_SENT',       -- Reminder notification sent
    'CHECKED_IN',          -- Patient checked in (for in-person)
    'IN_PROGRESS',         -- Consultation started
    'COMPLETED',           -- Consultation completed
    'CANCELLED_BY_PATIENT',
    'CANCELLED_BY_DOCTOR',
    'CANCELLED_SYSTEM',    -- Auto-cancelled (no payment, etc.)
    'NO_SHOW',             -- Patient didn't attend
    'RESCHEDULED'          -- Moved to different slot
);

-- Appointments table
CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- Participants
    patient_id UUID NOT NULL,      -- References user-service
    doctor_id UUID NOT NULL,       -- References doctor-service
    clinic_id UUID,                -- NULL for video consultations
    
    -- Timing
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 15,
    
    -- Type and status
    consultation_type consultation_type NOT NULL,
    status appointment_status NOT NULL DEFAULT 'PENDING_PAYMENT',
    
    -- Slot reference
    slot_id UUID,                  -- References available_slots
    
    -- Fees
    consultation_fee DECIMAL(10, 2) NOT NULL,
    platform_fee DECIMAL(10, 2) DEFAULT 0,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'INR',
    
    -- Payment
    payment_id UUID,               -- References payment-service
    payment_status VARCHAR(50),
    
    -- Booking details
    booking_notes TEXT,            -- Patient's notes for doctor
    
    -- Cancellation
    cancelled_at TIMESTAMP WITH TIME ZONE,
    cancelled_by UUID,             -- Who cancelled
    cancellation_reason TEXT,
    refund_amount DECIMAL(10, 2),
    refund_status VARCHAR(50),
    
    -- Rescheduling
    rescheduled_from_id UUID REFERENCES appointments(id),
    rescheduled_to_id UUID REFERENCES appointments(id),
    reschedule_count INT DEFAULT 0,
    
    -- Consultation details
    consultation_started_at TIMESTAMP WITH TIME ZONE,
    consultation_ended_at TIMESTAMP WITH TIME ZONE,
    consultation_id UUID,          -- References consultation-service
    
    -- Follow-up
    is_followup BOOLEAN DEFAULT FALSE,
    original_appointment_id UUID REFERENCES appointments(id),
    followup_scheduled BOOLEAN DEFAULT FALSE,
    
    -- Reservation expiry
    reserved_until TIMESTAMP WITH TIME ZONE,  -- When reservation expires
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Indexes
    CONSTRAINT valid_reservation CHECK (
        status != 'PENDING_PAYMENT' OR reserved_until IS NOT NULL
    )
);

-- Appointment state history (audit trail)
CREATE TABLE appointment_status_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    appointment_id UUID NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    from_status appointment_status,
    to_status appointment_status NOT NULL,
    changed_by UUID,               -- User who made the change
    reason TEXT,
    metadata JSONB,                -- Additional context
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_appointments_patient ON appointments(patient_id, scheduled_at);
CREATE INDEX idx_appointments_doctor ON appointments(doctor_id, scheduled_at);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_scheduled ON appointments(scheduled_at);
CREATE INDEX idx_appointments_pending ON appointments(status, reserved_until) 
    WHERE status = 'PENDING_PAYMENT';
CREATE INDEX idx_appointments_upcoming ON appointments(scheduled_at) 
    WHERE status IN ('CONFIRMED', 'REMINDER_SENT');
CREATE INDEX idx_status_history_appointment ON appointment_status_history(appointment_id);
```
</details>

<details>
<summary><strong>B6.4 - Reserve Slot Implementation</strong></summary>

```java
// BookingRequest.java
@Value
@Builder
public class BookingRequest {
    @NotNull String doctorId;
    @NotNull String slotId;
    @NotNull ConsultationType consultationType;
    String clinicId;            // Required for IN_PERSON
    String bookingNotes;        // Patient's notes
}

// AppointmentController.java
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    @PostMapping("/reserve")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ReservationResponse> reserveSlot(
            @RequestHeader("X-User-Id") String patientId,
            @Valid @RequestBody BookingRequest request) {
        return appointmentService.reserveSlot(patientId, request);
    }
    
    @PostMapping("/{appointmentId}/confirm")
    public Mono<AppointmentDto> confirmAppointment(
            @RequestHeader("X-User-Id") String patientId,
            @PathVariable String appointmentId,
            @RequestBody PaymentConfirmation paymentConfirmation) {
        return appointmentService.confirmAppointment(patientId, appointmentId, paymentConfirmation);
    }
    
    @PostMapping("/{appointmentId}/cancel")
    public Mono<AppointmentDto> cancelAppointment(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String appointmentId,
            @RequestBody CancellationRequest request) {
        return appointmentService.cancelAppointment(userId, appointmentId, request);
    }
    
    @GetMapping("/patient/me")
    public Flux<AppointmentDto> getPatientAppointments(
            @RequestHeader("X-User-Id") String patientId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return appointmentService.getPatientAppointments(patientId, status, fromDate, page, size);
    }
    
    @GetMapping("/doctor/me")
    public Flux<AppointmentDto> getDoctorAppointments(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) AppointmentStatus status) {
        return appointmentService.getDoctorAppointments(userId, date, status);
    }
}

// AppointmentService.java
@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepo;
    private final AvailableSlotRepository slotRepo;
    private final DoctorClient doctorClient;
    private final AppointmentEventPublisher eventPublisher;
    private final AppointmentStatusHistoryRepository historyRepo;
    
    private static final int RESERVATION_MINUTES = 10;  // Slot hold time
    
    public Mono<ReservationResponse> reserveSlot(String patientId, BookingRequest request) {
        return slotRepo.findById(request.getSlotId())
            .switchIfEmpty(Mono.error(new NotFoundException("Slot not found")))
            .flatMap(slot -> validateSlotAvailable(slot))
            .flatMap(slot -> doctorClient.getDoctorFees(request.getDoctorId()))
            .flatMap(fees -> {
                Appointment appointment = Appointment.builder()
                    .patientId(patientId)
                    .doctorId(request.getDoctorId())
                    .clinicId(request.getClinicId())
                    .slotId(request.getSlotId())
                    .scheduledAt(combineDateTime(slot.getSlotDate(), slot.getStartTime()))
                    .durationMinutes(slot.getSlotDurationMinutes())
                    .consultationType(request.getConsultationType())
                    .status(AppointmentStatus.PENDING_PAYMENT)
                    .consultationFee(fees.getConsultationFee())
                    .platformFee(calculatePlatformFee(fees.getConsultationFee()))
                    .totalAmount(fees.getConsultationFee().add(platformFee))
                    .bookingNotes(request.getBookingNotes())
                    .reservedUntil(Instant.now().plusSeconds(RESERVATION_MINUTES * 60))
                    .build();
                    
                return appointmentRepo.save(appointment);
            })
            .flatMap(appointment -> 
                slotRepo.updateStatus(request.getSlotId(), SlotStatus.RESERVED, appointment.getId())
                    .thenReturn(appointment))
            .flatMap(appointment -> 
                saveStatusHistory(appointment, null, AppointmentStatus.PENDING_PAYMENT, patientId)
                    .thenReturn(appointment))
            .doOnSuccess(appointment -> eventPublisher.publishReserved(appointment))
            .map(appointment -> ReservationResponse.builder()
                .appointmentId(appointment.getId())
                .doctorId(appointment.getDoctorId())
                .scheduledAt(appointment.getScheduledAt())
                .consultationType(appointment.getConsultationType())
                .totalAmount(appointment.getTotalAmount())
                .currency(appointment.getCurrency())
                .reservedUntil(appointment.getReservedUntil())
                .expiresInSeconds(RESERVATION_MINUTES * 60)
                .build());
    }
    
    private Mono<AvailableSlot> validateSlotAvailable(AvailableSlot slot) {
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            return Mono.error(new BusinessException("Slot is no longer available"));
        }
        if (slot.getSlotDate().isBefore(LocalDate.now())) {
            return Mono.error(new BusinessException("Cannot book past slots"));
        }
        return Mono.just(slot);
    }
    
    public Mono<AppointmentDto> confirmAppointment(
            String patientId, 
            String appointmentId, 
            PaymentConfirmation payment) {
        
        return appointmentRepo.findById(appointmentId)
            .switchIfEmpty(Mono.error(new NotFoundException("Appointment not found")))
            .flatMap(appointment -> validateConfirmation(appointment, patientId))
            .flatMap(appointment -> {
                appointment = appointment.toBuilder()
                    .status(AppointmentStatus.CONFIRMED)
                    .paymentId(payment.getPaymentId())
                    .paymentStatus(payment.getStatus())
                    .reservedUntil(null)  // Clear reservation
                    .build();
                    
                return appointmentRepo.save(appointment);
            })
            .flatMap(appointment ->
                slotRepo.updateStatus(appointment.getSlotId(), SlotStatus.BOOKED, appointment.getId())
                    .thenReturn(appointment))
            .flatMap(appointment ->
                saveStatusHistory(appointment, AppointmentStatus.PENDING_PAYMENT, 
                    AppointmentStatus.CONFIRMED, patientId)
                    .thenReturn(appointment))
            .doOnSuccess(appointment -> eventPublisher.publishConfirmed(appointment))
            .map(this::toDto);
    }
    
    private Mono<Appointment> validateConfirmation(Appointment appointment, String patientId) {
        if (!appointment.getPatientId().equals(patientId)) {
            return Mono.error(new UnauthorizedException("Not authorized"));
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            return Mono.error(new BusinessException("Invalid appointment status"));
        }
        if (appointment.getReservedUntil().isBefore(Instant.now())) {
            return Mono.error(new BusinessException("Reservation expired"));
        }
        return Mono.just(appointment);
    }
}

// ReservationResponse.java
@Value
@Builder
public class ReservationResponse {
    String appointmentId;
    String doctorId;
    Instant scheduledAt;
    ConsultationType consultationType;
    BigDecimal totalAmount;
    String currency;
    Instant reservedUntil;
    long expiresInSeconds;
}
```
</details>

<details>
<summary><strong>B6.12 - Expiry Handler (Scheduled Job)</strong></summary>

```java
// ReservationExpiryHandler.java
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpiryHandler {
    
    private final AppointmentRepository appointmentRepo;
    private final AvailableSlotRepository slotRepo;
    private final AppointmentEventPublisher eventPublisher;
    
    @Scheduled(fixedRate = 60000)  // Run every minute
    public void handleExpiredReservations() {
        log.info("Checking for expired reservations");
        
        appointmentRepo.findExpiredReservations(Instant.now())
            .flatMap(this::releaseReservation)
            .subscribe(
                appointment -> log.info("Released expired reservation: {}", appointment.getId()),
                error -> log.error("Error releasing reservation", error)
            );
    }
    
    private Mono<Appointment> releaseReservation(Appointment appointment) {
        return Mono.defer(() -> {
            Appointment updated = appointment.toBuilder()
                .status(AppointmentStatus.CANCELLED_SYSTEM)
                .cancellationReason("Reservation expired - payment not completed")
                .cancelledAt(Instant.now())
                .build();
                
            return appointmentRepo.save(updated);
        })
        .flatMap(updated -> 
            slotRepo.updateStatus(updated.getSlotId(), SlotStatus.AVAILABLE, null)
                .thenReturn(updated))
        .doOnSuccess(updated -> eventPublisher.publishExpired(updated));
    }
}

// AppointmentRepository.java
@Repository
public interface AppointmentRepository extends ReactiveCrudRepository<Appointment, String> {
    
    @Query("""
        SELECT * FROM appointments 
        WHERE status = 'PENDING_PAYMENT' 
        AND reserved_until < :now
        """)
    Flux<Appointment> findExpiredReservations(Instant now);
    
    @Query("""
        SELECT * FROM appointments 
        WHERE patient_id = :patientId 
        AND (:status IS NULL OR status = :status)
        AND (:fromDate IS NULL OR scheduled_at >= :fromDate)
        ORDER BY scheduled_at DESC
        LIMIT :size OFFSET :offset
        """)
    Flux<Appointment> findByPatientId(
        String patientId, 
        AppointmentStatus status, 
        LocalDate fromDate,
        int size, 
        int offset);
    
    @Query("""
        SELECT * FROM appointments 
        WHERE doctor_id = :doctorId 
        AND (:date IS NULL OR DATE(scheduled_at) = :date)
        AND (:status IS NULL OR status = :status)
        ORDER BY scheduled_at ASC
        """)
    Flux<Appointment> findByDoctorId(
        String doctorId, 
        LocalDate date, 
        AppointmentStatus status);
}
```
</details>

---

#### Frontend Tasks - Sprint 4

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| F4.1 | Availability Calendar (Doctor) | Weekly schedule configuration UI | Frontend 2 | 24 | P0 | Can set weekly schedule |
| F4.2 | Slot Configuration | Duration, buffer, consultation type | Frontend 2 | 12 | P0 | All options configurable |
| F4.3 | Block Time UI | Mark days/times as unavailable | Frontend 2 | 8 | P0 | Can block slots |
| F4.4 | Booking Calendar (Patient) | Date picker on doctor profile | Frontend 1 | 16 | P0 | Shows available dates |
| F4.5 | Time Slot Picker | Show available times for selected date | Frontend 1 | 12 | P0 | Shows available slots |
| F4.6 | Booking Summary | Review before payment | Frontend 1 | 8 | P0 | Summary displays correctly |
| F4.7 | My Appointments (Patient) | List with status filters | Frontend 1 | 12 | P0 | Appointments list works |
| F4.8 | Appointment Card | Status, doctor info, actions | Frontend 1 | 8 | P0 | Card displays correctly |
| F4.9 | Appointments Dashboard (Doctor) | Today's schedule, calendar view | Frontend 2 | 16 | P0 | Dashboard works |
| F4.10 | Appointment Details Modal | Full details, actions | Frontend 1 | 8 | P0 | Modal works |

**Frontend Implementation:**

<details>
<summary><strong>F4.1 - Availability Calendar (Doctor Dashboard)</strong></summary>

```typescript
// components/doctor/AvailabilityCalendar.tsx
'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardHeader, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Select } from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { TimePicker } from '@/components/ui/time-picker';
import { availabilityApi } from '@/lib/api/availability';
import { WeeklyAvailability, ConsultationType } from '@/types/availability';
import { Plus, Trash2, Copy } from 'lucide-react';

const DAYS = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

export function AvailabilityCalendar() {
  const queryClient = useQueryClient();
  const [selectedDay, setSelectedDay] = useState<number | null>(null);
  
  const { data: weeklySlots, isLoading } = useQuery({
    queryKey: ['availability', 'weekly'],
    queryFn: availabilityApi.getWeeklySchedule,
  });
  
  const addSlotMutation = useMutation({
    mutationFn: availabilityApi.addWeeklySlot,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['availability'] });
    },
  });
  
  const deleteSlotMutation = useMutation({
    mutationFn: availabilityApi.deleteWeeklySlot,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['availability'] });
    },
  });
  
  // Group slots by day
  const slotsByDay = weeklySlots?.reduce((acc, slot) => {
    const day = slot.dayOfWeek;
    if (!acc[day]) acc[day] = [];
    acc[day].push(slot);
    return acc;
  }, {} as Record<number, WeeklyAvailability[]>) || {};
  
  return (
    <Card>
      <CardHeader>
        <h2 className="text-xl font-semibold">Weekly Availability</h2>
        <p className="text-gray-500">Set your recurring weekly schedule</p>
      </CardHeader>
      
      <CardContent>
        <div className="space-y-4">
          {DAYS.map((day, index) => (
            <DaySchedule
              key={day}
              dayName={day}
              dayIndex={index}
              slots={slotsByDay[index] || []}
              onAddSlot={(slot) => addSlotMutation.mutate({ ...slot, dayOfWeek: index })}
              onDeleteSlot={(slotId) => deleteSlotMutation.mutate(slotId)}
              onCopyToDay={(targetDay) => copyDaySchedule(index, targetDay)}
            />
          ))}
        </div>
        
        <div className="mt-6 p-4 bg-blue-50 rounded-lg">
          <h3 className="font-medium text-blue-800">Quick Actions</h3>
          <div className="mt-2 flex gap-2">
            <Button variant="outline" size="sm" onClick={copyWeekdaySchedule}>
              Copy Weekday Schedule
            </Button>
            <Button variant="outline" size="sm" onClick={clearAllSlots}>
              Clear All
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

interface DayScheduleProps {
  dayName: string;
  dayIndex: number;
  slots: WeeklyAvailability[];
  onAddSlot: (slot: Partial<WeeklyAvailability>) => void;
  onDeleteSlot: (slotId: string) => void;
  onCopyToDay: (targetDay: number) => void;
}

function DaySchedule({ dayName, dayIndex, slots, onAddSlot, onDeleteSlot }: DayScheduleProps) {
  const [isAdding, setIsAdding] = useState(false);
  const [newSlot, setNewSlot] = useState({
    startTime: '09:00',
    endTime: '17:00',
    slotDurationMinutes: 15,
    bufferMinutes: 5,
    consultationType: 'IN_PERSON' as ConsultationType,
  });
  
  const handleAddSlot = () => {
    onAddSlot(newSlot);
    setIsAdding(false);
    setNewSlot({
      startTime: '09:00',
      endTime: '17:00',
      slotDurationMinutes: 15,
      bufferMinutes: 5,
      consultationType: 'IN_PERSON',
    });
  };
  
  return (
    <div className="border rounded-lg p-4">
      <div className="flex items-center justify-between mb-3">
        <h3 className="font-medium">{dayName}</h3>
        <div className="flex items-center gap-2">
          {slots.length > 0 && (
            <span className="text-sm text-green-600">
              {slots.length} slot{slots.length > 1 ? 's' : ''} configured
            </span>
          )}
          <Button 
            variant="ghost" 
            size="sm"
            onClick={() => setIsAdding(true)}
          >
            <Plus className="w-4 h-4 mr-1" />
            Add Slot
          </Button>
        </div>
      </div>
      
      {/* Existing slots */}
      <div className="space-y-2">
        {slots.map((slot) => (
          <div 
            key={slot.id} 
            className="flex items-center justify-between bg-gray-50 p-3 rounded"
          >
            <div className="flex items-center gap-4">
              <span className="font-mono">
                {slot.startTime} - {slot.endTime}
              </span>
              <span className="text-sm text-gray-500">
                {slot.slotDurationMinutes} min slots
              </span>
              <span className={`text-xs px-2 py-1 rounded ${
                slot.consultationType === 'VIDEO' 
                  ? 'bg-blue-100 text-blue-700'
                  : 'bg-green-100 text-green-700'
              }`}>
                {slot.consultationType}
              </span>
            </div>
            <Button 
              variant="ghost" 
              size="sm"
              onClick={() => onDeleteSlot(slot.id)}
            >
              <Trash2 className="w-4 h-4 text-red-500" />
            </Button>
          </div>
        ))}
      </div>
      
      {/* Add new slot form */}
      {isAdding && (
        <div className="mt-3 p-4 border-2 border-dashed rounded-lg">
          <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
            <TimePicker
              label="Start Time"
              value={newSlot.startTime}
              onChange={(time) => setNewSlot({ ...newSlot, startTime: time })}
            />
            <TimePicker
              label="End Time"
              value={newSlot.endTime}
              onChange={(time) => setNewSlot({ ...newSlot, endTime: time })}
            />
            <Select
              label="Duration"
              value={newSlot.slotDurationMinutes.toString()}
              onChange={(v) => setNewSlot({ ...newSlot, slotDurationMinutes: parseInt(v) })}
              options={[
                { value: '10', label: '10 min' },
                { value: '15', label: '15 min' },
                { value: '20', label: '20 min' },
                { value: '30', label: '30 min' },
                { value: '45', label: '45 min' },
                { value: '60', label: '60 min' },
              ]}
            />
            <Select
              label="Type"
              value={newSlot.consultationType}
              onChange={(v) => setNewSlot({ ...newSlot, consultationType: v as ConsultationType })}
              options={[
                { value: 'IN_PERSON', label: 'In-Person' },
                { value: 'VIDEO', label: 'Video' },
                { value: 'BOTH', label: 'Both' },
              ]}
            />
            <div className="flex items-end gap-2">
              <Button onClick={handleAddSlot}>Save</Button>
              <Button variant="outline" onClick={() => setIsAdding(false)}>Cancel</Button>
            </div>
          </div>
        </div>
      )}
      
      {slots.length === 0 && !isAdding && (
        <p className="text-sm text-gray-400 italic">No availability set for this day</p>
      )}
    </div>
  );
}
```
</details>

<details>
<summary><strong>F4.4 & F4.5 - Booking Calendar & Slot Picker (Patient App)</strong></summary>

```typescript
// components/doctors/BookingSection.tsx
'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { format, addDays, isSameDay } from 'date-fns';
import { Card, CardHeader, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { slotsApi } from '@/lib/api/slots';
import { DoctorProfile, ConsultationType, SlotDto } from '@/types/doctor';
import { ChevronLeft, ChevronRight, Video, MapPin, Clock } from 'lucide-react';

interface BookingSectionProps {
  doctor: DoctorProfile;
  onSlotSelected: (slot: SlotDto, consultationType: ConsultationType) => void;
}

export function BookingSection({ doctor, onSlotSelected }: BookingSectionProps) {
  const [consultationType, setConsultationType] = useState<ConsultationType>(
    doctor.availableForVideoConsultation ? 'VIDEO' : 'IN_PERSON'
  );
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [dateRangeStart, setDateRangeStart] = useState<Date>(new Date());
  
  // Generate date range to display (7 days)
  const dateRange = Array.from({ length: 7 }, (_, i) => addDays(dateRangeStart, i));
  
  // Fetch available slots
  const { data: slotsData, isLoading } = useQuery({
    queryKey: ['slots', doctor.id, format(dateRangeStart, 'yyyy-MM-dd'), consultationType],
    queryFn: () => slotsApi.getAvailableSlots({
      doctorId: doctor.id,
      startDate: format(dateRangeStart, 'yyyy-MM-dd'),
      endDate: format(addDays(dateRangeStart, 7), 'yyyy-MM-dd'),
      consultationType,
    }),
  });
  
  const slotsForSelectedDate = slotsData?.days.find(
    day => isSameDay(new Date(day.date), selectedDate)
  )?.slots || [];
  
  const handlePrevWeek = () => {
    const newStart = addDays(dateRangeStart, -7);
    if (newStart >= new Date()) {
      setDateRangeStart(newStart);
    }
  };
  
  const handleNextWeek = () => {
    setDateRangeStart(addDays(dateRangeStart, 7));
  };
  
  return (
    <Card>
      <CardHeader>
        <h2 className="text-xl font-semibold">Book Appointment</h2>
      </CardHeader>
      
      <CardContent className="space-y-6">
        {/* Consultation Type Selection */}
        <div>
          <label className="text-sm font-medium text-gray-700 mb-2 block">
            Consultation Type
          </label>
          <RadioGroup
            value={consultationType}
            onValueChange={(v) => setConsultationType(v as ConsultationType)}
            className="flex gap-4"
          >
            {doctor.availableForVideoConsultation && (
              <label className={`flex items-center gap-2 p-3 border rounded-lg cursor-pointer
                ${consultationType === 'VIDEO' ? 'border-primary-500 bg-primary-50' : ''}`}>
                <RadioGroupItem value="VIDEO" />
                <Video className="w-5 h-5" />
                <div>
                  <p className="font-medium">Video Consultation</p>
                  <p className="text-sm text-gray-500">₹{doctor.videoConsultationFee}</p>
                </div>
              </label>
            )}
            {doctor.clinics?.length > 0 && (
              <label className={`flex items-center gap-2 p-3 border rounded-lg cursor-pointer
                ${consultationType === 'IN_PERSON' ? 'border-primary-500 bg-primary-50' : ''}`}>
                <RadioGroupItem value="IN_PERSON" />
                <MapPin className="w-5 h-5" />
                <div>
                  <p className="font-medium">In-Person Visit</p>
                  <p className="text-sm text-gray-500">₹{doctor.consultationFee}</p>
                </div>
              </label>
            )}
          </RadioGroup>
        </div>
        
        {/* Date Selection */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <label className="text-sm font-medium text-gray-700">
              Select Date
            </label>
            <div className="flex gap-2">
              <Button 
                variant="ghost" 
                size="sm"
                onClick={handlePrevWeek}
                disabled={isSameDay(dateRangeStart, new Date())}
              >
                <ChevronLeft className="w-4 h-4" />
              </Button>
              <Button variant="ghost" size="sm" onClick={handleNextWeek}>
                <ChevronRight className="w-4 h-4" />
              </Button>
            </div>
          </div>
          
          <div className="grid grid-cols-7 gap-2">
            {dateRange.map((date) => {
              const daySlots = slotsData?.days.find(d => isSameDay(new Date(d.date), date));
              const hasSlots = daySlots && daySlots.slots.length > 0;
              const isSelected = isSameDay(date, selectedDate);
              const isPast = date < new Date() && !isSameDay(date, new Date());
              
              return (
                <button
                  key={date.toISOString()}
                  onClick={() => setSelectedDate(date)}
                  disabled={!hasSlots || isPast}
                  className={`p-3 rounded-lg text-center transition-colors
                    ${isSelected ? 'bg-primary-500 text-white' : ''}
                    ${hasSlots && !isSelected ? 'bg-green-50 hover:bg-green-100' : ''}
                    ${!hasSlots || isPast ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : ''}
                  `}
                >
                  <p className="text-xs font-medium">
                    {format(date, 'EEE')}
                  </p>
                  <p className="text-lg font-bold">
                    {format(date, 'd')}
                  </p>
                  <p className="text-xs">
                    {format(date, 'MMM')}
                  </p>
                  {hasSlots && (
                    <p className="text-xs mt-1 text-green-600">
                      {daySlots.slots.length} slots
                    </p>
                  )}
                </button>
              );
            })}
          </div>
        </div>
        
        {/* Time Slot Selection */}
        <div>
          <label className="text-sm font-medium text-gray-700 mb-3 block">
            Available Slots for {format(selectedDate, 'EEEE, MMMM d')}
          </label>
          
          {isLoading ? (
            <div className="grid grid-cols-4 gap-2">
              {[...Array(8)].map((_, i) => (
                <div key={i} className="h-10 bg-gray-100 animate-pulse rounded" />
              ))}
            </div>
          ) : slotsForSelectedDate.length > 0 ? (
            <div className="grid grid-cols-4 gap-2">
              {slotsForSelectedDate.map((slot) => (
                <button
                  key={slot.slotId}
                  onClick={() => onSlotSelected(slot, consultationType)}
                  className="p-2 border rounded-lg hover:border-primary-500 hover:bg-primary-50 
                    transition-colors text-center"
                >
                  <Clock className="w-4 h-4 mx-auto mb-1 text-gray-400" />
                  <span className="text-sm font-medium">
                    {slot.startTime}
                  </span>
                </button>
              ))}
            </div>
          ) : (
            <p className="text-gray-500 text-center py-8">
              No slots available for this date
            </p>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
```
</details>

<details>
<summary><strong>F4.7 - My Appointments Page (Patient)</strong></summary>

```typescript
// app/appointments/page.tsx
'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';
import { AppointmentCard } from '@/components/appointments/AppointmentCard';
import { EmptyState } from '@/components/ui/empty-state';
import { Skeleton } from '@/components/ui/skeleton';
import { appointmentsApi } from '@/lib/api/appointments';
import { AppointmentStatus } from '@/types/appointment';
import { Calendar, Clock, CheckCircle } from 'lucide-react';

export default function MyAppointmentsPage() {
  const [activeTab, setActiveTab] = useState<'upcoming' | 'past' | 'cancelled'>('upcoming');
  
  const { data: appointments, isLoading } = useQuery({
    queryKey: ['appointments', activeTab],
    queryFn: () => appointmentsApi.getMyAppointments({
      status: getStatusForTab(activeTab),
    }),
  });
  
  function getStatusForTab(tab: string): AppointmentStatus[] | undefined {
    switch (tab) {
      case 'upcoming':
        return ['CONFIRMED', 'REMINDER_SENT', 'CHECKED_IN'];
      case 'past':
        return ['COMPLETED', 'NO_SHOW'];
      case 'cancelled':
        return ['CANCELLED_BY_PATIENT', 'CANCELLED_BY_DOCTOR', 'CANCELLED_SYSTEM'];
      default:
        return undefined;
    }
  }
  
  return (
    <div className="container mx-auto py-8">
      <h1 className="text-2xl font-bold mb-6">My Appointments</h1>
      
      <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as typeof activeTab)}>
        <TabsList className="mb-6">
          <TabsTrigger value="upcoming">
            <Clock className="w-4 h-4 mr-2" />
            Upcoming
          </TabsTrigger>
          <TabsTrigger value="past">
            <CheckCircle className="w-4 h-4 mr-2" />
            Past
          </TabsTrigger>
          <TabsTrigger value="cancelled">
            <Calendar className="w-4 h-4 mr-2" />
            Cancelled
          </TabsTrigger>
        </TabsList>
        
        <TabsContent value={activeTab}>
          {isLoading ? (
            <div className="space-y-4">
              {[...Array(3)].map((_, i) => (
                <Skeleton key={i} className="h-40 w-full" />
              ))}
            </div>
          ) : appointments?.length > 0 ? (
            <div className="space-y-4">
              {appointments.map((appointment) => (
                <AppointmentCard 
                  key={appointment.id} 
                  appointment={appointment}
                  showActions={activeTab === 'upcoming'}
                />
              ))}
            </div>
          ) : (
            <EmptyState
              icon={<Calendar className="w-12 h-12" />}
              title={`No ${activeTab} appointments`}
              description={
                activeTab === 'upcoming' 
                  ? "You don't have any upcoming appointments. Book one now!"
                  : `You don't have any ${activeTab} appointments.`
              }
              action={
                activeTab === 'upcoming' && (
                  <Button href="/doctors">Find a Doctor</Button>
                )
              }
            />
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
```
</details>

---

### Sprint 4 Deliverables Checklist

- [ ] **Doctor Availability**
  - [ ] Weekly schedule CRUD
  - [ ] Block time functionality
  - [ ] Available slots generation
  - [ ] Slots API returning correct data

- [ ] **Appointment Service**
  - [ ] Slot reservation (10-min hold)
  - [ ] Appointment confirmation
  - [ ] Cancellation with reason
  - [ ] Patient appointments list
  - [ ] Doctor appointments list
  - [ ] Expired reservation cleanup

- [ ] **Frontend - Patient App**
  - [ ] Booking calendar component
  - [ ] Time slot picker
  - [ ] My appointments page
  - [ ] Appointment cards with status

- [ ] **Frontend - Doctor Dashboard**
  - [ ] Availability calendar
  - [ ] Slot configuration
  - [ ] Appointments dashboard

---

### Sprint 5 (Week 9-10): Payment & Notification Services

**Sprint Goal**: Complete payment integration with Saga pattern; Notifications sent for all appointment events.

---

#### Payment Service Tasks - Sprint 5

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B8.1 | Create Payment Service | Project setup with security focus | Backend 3 | 4 | P0 | Service runs |
| B8.2 | Payments Schema | Transactions, refunds tables | Backend 3 | 8 | P0 | Migrations run |
| B8.3 | Payment Entity & Repository | R2DBC models | Backend 3 | 8 | P0 | Repository works |
| B8.4 | Razorpay Integration | SDK setup, order creation | Backend 3 | 16 | P0 | Can create orders |
| B8.5 | Payment Initiation | POST /payments/initiate | Backend 3 | 8 | P0 | Returns payment link |
| B8.6 | Webhook Handler | Handle Razorpay callbacks | Backend 3 | 12 | P0 | Webhooks processed |
| B8.7 | Payment Verification | Verify payment signature | Backend 3 | 8 | P0 | Verification works |
| B8.8 | Payment Events | Publish to Kafka | Backend 3 | 4 | P0 | Events published |
| B8.9 | Refund Processing | Full and partial refunds | Backend 3 | 12 | P0 | Refunds work |
| B8.10 | Payment History | GET /payments/history | Backend 3 | 4 | P0 | History returns |
| B8.11 | Doctor Payouts Schema | Payout tracking | Backend 3 | 8 | P1 | Schema ready |
| B8.12 | Unit & Integration Tests | Security-focused tests | Backend 3 | 12 | P0 | Tests pass |

**Payment Service Implementation:**

<details>
<summary><strong>B8.2 - Payments Database Schema</strong></summary>

```sql
-- V1__create_payments_tables.sql

-- Payment status
CREATE TYPE payment_status AS ENUM (
    'INITIATED',
    'PENDING',
    'AUTHORIZED',
    'CAPTURED',
    'COMPLETED',
    'FAILED',
    'CANCELLED',
    'REFUND_INITIATED',
    'PARTIALLY_REFUNDED',
    'REFUNDED'
);

-- Payment method
CREATE TYPE payment_method AS ENUM (
    'CARD',
    'UPI',
    'NET_BANKING',
    'WALLET',
    'EMI',
    'PAY_LATER'
);

-- Payment transactions
CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- Reference
    order_type VARCHAR(50) NOT NULL,  -- APPOINTMENT, ORDER, SUBSCRIPTION
    order_id UUID NOT NULL,            -- References the order (appointment_id, etc.)
    user_id UUID NOT NULL,
    
    -- Amount
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    
    -- Payment details
    payment_method payment_method,
    status payment_status NOT NULL DEFAULT 'INITIATED',
    
    -- Gateway details
    gateway VARCHAR(50) NOT NULL DEFAULT 'RAZORPAY',
    gateway_order_id VARCHAR(100),     -- Razorpay order_id
    gateway_payment_id VARCHAR(100),   -- Razorpay payment_id
    gateway_signature VARCHAR(255),    -- For verification
    
    -- Response
    gateway_response JSONB,
    failure_reason TEXT,
    
    -- Refund tracking
    refunded_amount DECIMAL(10, 2) DEFAULT 0,
    
    -- Metadata
    description TEXT,
    metadata JSONB,                    -- Additional data
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    
    -- Idempotency
    idempotency_key VARCHAR(100) UNIQUE
);

-- Refunds
CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id UUID NOT NULL REFERENCES payment_transactions(id),
    
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    
    reason VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'INITIATED',
    
    -- Gateway
    gateway_refund_id VARCHAR(100),
    gateway_response JSONB,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    processed_at TIMESTAMP WITH TIME ZONE
);

-- Doctor payouts
CREATE TABLE doctor_payouts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID NOT NULL,
    
    -- Period
    payout_period_start DATE NOT NULL,
    payout_period_end DATE NOT NULL,
    
    -- Amount
    gross_amount DECIMAL(10, 2) NOT NULL,      -- Total consultation fees
    platform_fee DECIMAL(10, 2) NOT NULL,       -- Platform commission
    tax_deducted DECIMAL(10, 2) DEFAULT 0,      -- TDS
    net_amount DECIMAL(10, 2) NOT NULL,         -- Amount to pay
    currency VARCHAR(3) DEFAULT 'INR',
    
    -- Status
    status VARCHAR(50) DEFAULT 'PENDING',  -- PENDING, PROCESSING, COMPLETED, FAILED
    
    -- Bank details
    bank_account_id UUID,
    
    -- Gateway
    payout_reference VARCHAR(100),
    gateway_response JSONB,
    
    -- Appointments included
    appointment_count INT,
    appointment_ids UUID[],
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    processed_at TIMESTAMP WITH TIME ZONE,
    
    UNIQUE(doctor_id, payout_period_start, payout_period_end)
);

-- Indexes
CREATE INDEX idx_payments_order ON payment_transactions(order_type, order_id);
CREATE INDEX idx_payments_user ON payment_transactions(user_id);
CREATE INDEX idx_payments_status ON payment_transactions(status);
CREATE INDEX idx_payments_gateway_order ON payment_transactions(gateway_order_id);
CREATE INDEX idx_refunds_payment ON refunds(payment_id);
CREATE INDEX idx_payouts_doctor ON doctor_payouts(doctor_id, status);
```
</details>

<details>
<summary><strong>B8.4-B8.7 - Razorpay Integration</strong></summary>

```java
// RazorpayConfig.java
@Configuration
public class RazorpayConfig {
    
    @Value("${razorpay.key-id}")
    private String keyId;
    
    @Value("${razorpay.key-secret}")
    private String keySecret;
    
    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }
}

// PaymentService.java
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepo;
    private final RazorpayClient razorpayClient;
    private final PaymentEventPublisher eventPublisher;
    
    @Value("${razorpay.key-id}")
    private String razorpayKeyId;
    
    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;
    
    public Mono<PaymentInitiationResponse> initiatePayment(PaymentRequest request) {
        return Mono.fromCallable(() -> {
            // Create Razorpay order
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.getAmount().multiply(new BigDecimal(100)).intValue()); // In paise
            orderRequest.put("currency", request.getCurrency());
            orderRequest.put("receipt", request.getOrderId());
            orderRequest.put("payment_capture", 1);  // Auto-capture
            
            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            return razorpayOrder;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(razorpayOrder -> {
            PaymentTransaction payment = PaymentTransaction.builder()
                .orderType(request.getOrderType())
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.INITIATED)
                .gateway("RAZORPAY")
                .gatewayOrderId(razorpayOrder.get("id"))
                .description(request.getDescription())
                .idempotencyKey(UUID.randomUUID().toString())
                .build();
                
            return paymentRepo.save(payment)
                .map(saved -> PaymentInitiationResponse.builder()
                    .paymentId(saved.getId())
                    .gatewayOrderId(razorpayOrder.get("id"))
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .razorpayKeyId(razorpayKeyId)
                    .prefill(buildPrefill(request))
                    .build());
        })
        .doOnSuccess(response -> log.info("Payment initiated: {}", response.getPaymentId()));
    }
    
    public Mono<PaymentTransaction> handleWebhook(RazorpayWebhookEvent event) {
        return Mono.defer(() -> {
            String eventType = event.getEvent();
            JSONObject payload = event.getPayload();
            
            switch (eventType) {
                case "payment.captured":
                    return handlePaymentCaptured(payload);
                case "payment.failed":
                    return handlePaymentFailed(payload);
                case "refund.processed":
                    return handleRefundProcessed(payload);
                default:
                    log.warn("Unhandled webhook event: {}", eventType);
                    return Mono.empty();
            }
        });
    }
    
    private Mono<PaymentTransaction> handlePaymentCaptured(JSONObject payload) {
        String gatewayPaymentId = payload.getJSONObject("payment").getJSONObject("entity").getString("id");
        String gatewayOrderId = payload.getJSONObject("payment").getJSONObject("entity").getString("order_id");
        
        return paymentRepo.findByGatewayOrderId(gatewayOrderId)
            .flatMap(payment -> {
                payment = payment.toBuilder()
                    .status(PaymentStatus.COMPLETED)
                    .gatewayPaymentId(gatewayPaymentId)
                    .gatewayResponse(payload.toString())
                    .completedAt(Instant.now())
                    .build();
                    
                return paymentRepo.save(payment);
            })
            .doOnSuccess(payment -> eventPublisher.publishPaymentCompleted(payment));
    }
    
    public Mono<Boolean> verifyPaymentSignature(PaymentVerificationRequest request) {
        return Mono.fromCallable(() -> {
            String payload = request.getGatewayOrderId() + "|" + request.getGatewayPaymentId();
            String expectedSignature = HmacUtils.hmacSha256Hex(razorpayKeySecret, payload);
            return expectedSignature.equals(request.getGatewaySignature());
        });
    }
    
    public Mono<Refund> processRefund(RefundRequest request) {
        return paymentRepo.findById(request.getPaymentId())
            .switchIfEmpty(Mono.error(new NotFoundException("Payment not found")))
            .flatMap(payment -> {
                if (payment.getStatus() != PaymentStatus.COMPLETED) {
                    return Mono.error(new BusinessException("Payment not eligible for refund"));
                }
                
                BigDecimal refundableAmount = payment.getAmount()
                    .subtract(payment.getRefundedAmount());
                    
                if (request.getAmount().compareTo(refundableAmount) > 0) {
                    return Mono.error(new BusinessException("Refund amount exceeds refundable amount"));
                }
                
                return createRazorpayRefund(payment, request);
            })
            .doOnSuccess(refund -> eventPublisher.publishRefundProcessed(refund));
    }
    
    private Mono<Refund> createRazorpayRefund(PaymentTransaction payment, RefundRequest request) {
        return Mono.fromCallable(() -> {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", request.getAmount().multiply(new BigDecimal(100)).intValue());
            refundRequest.put("speed", "normal");
            
            com.razorpay.Refund razorpayRefund = razorpayClient.payments
                .refund(payment.getGatewayPaymentId(), refundRequest);
                
            return razorpayRefund;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(razorpayRefund -> {
            Refund refund = Refund.builder()
                .paymentId(payment.getId())
                .amount(request.getAmount())
                .currency(payment.getCurrency())
                .reason(request.getReason())
                .status("PROCESSING")
                .gatewayRefundId(razorpayRefund.get("id"))
                .build();
                
            return refundRepo.save(refund);
        });
    }
}

// PaymentController.java
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/initiate")
    public Mono<PaymentInitiationResponse> initiatePayment(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody PaymentRequest request) {
        request = request.toBuilder().userId(userId).build();
        return paymentService.initiatePayment(request);
    }
    
    @PostMapping("/verify")
    public Mono<PaymentVerificationResponse> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request) {
        return paymentService.verifyPaymentSignature(request)
            .map(valid -> PaymentVerificationResponse.builder()
                .valid(valid)
                .message(valid ? "Payment verified" : "Invalid signature")
                .build());
    }
    
    @PostMapping("/webhook")
    public Mono<ResponseEntity<Void>> handleWebhook(
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestBody String payload) {
        // Verify webhook signature first
        return paymentService.verifyWebhookSignature(payload, signature)
            .filter(valid -> valid)
            .flatMap(valid -> {
                RazorpayWebhookEvent event = parseWebhookEvent(payload);
                return paymentService.handleWebhook(event);
            })
            .thenReturn(ResponseEntity.ok().build())
            .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    @PostMapping("/{paymentId}/refund")
    public Mono<Refund> refundPayment(
            @PathVariable String paymentId,
            @Valid @RequestBody RefundRequest request) {
        request = request.toBuilder().paymentId(paymentId).build();
        return paymentService.processRefund(request);
    }
    
    @GetMapping("/history")
    public Flux<PaymentTransaction> getPaymentHistory(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paymentService.getPaymentHistory(userId, page, size);
    }
}
```
</details>

---

#### Booking Saga Tasks - Sprint 5

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B9.1 | Saga Orchestrator | Generic saga state machine | Tech Lead | 16 | P0 | Orchestrator works |
| B9.2 | Booking Saga | Reserve → Pay → Confirm flow | Backend 1 | 16 | P0 | Full flow works |
| B9.3 | Compensation Handlers | Rollback for each step | Backend 1 | 12 | P0 | Rollback works |
| B9.4 | Saga Persistence | Store saga state for recovery | Backend 1 | 8 | P0 | State persisted |
| B9.5 | Idempotency | Handle duplicate events | Backend 1 | 8 | P0 | Duplicates handled |
| B9.6 | Timeout Handling | Handle step timeouts | Backend 1 | 8 | P0 | Timeouts trigger compensation |

**Saga Pattern Implementation:**

<details>
<summary><strong>B9.1-B9.3 - Booking Saga Implementation</strong></summary>

```java
// Saga State Machine
public enum BookingSagaState {
    INITIATED,
    SLOT_RESERVED,
    PAYMENT_INITIATED,
    PAYMENT_COMPLETED,
    APPOINTMENT_CONFIRMED,
    COMPLETED,
    COMPENSATING,
    COMPENSATION_COMPLETED,
    FAILED
}

// BookingSaga.java
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingSaga {
    
    private final AppointmentService appointmentService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final SagaStateRepository sagaRepo;
    
    public Mono<BookingResult> execute(BookingCommand command) {
        String sagaId = UUID.randomUUID().toString();
        
        return initializeSaga(sagaId, command)
            .flatMap(saga -> executeStep(saga, BookingSagaState.INITIATED, this::reserveSlot))
            .flatMap(saga -> executeStep(saga, BookingSagaState.SLOT_RESERVED, this::initiatePayment))
            .flatMap(saga -> waitForPayment(saga))
            .flatMap(saga -> executeStep(saga, BookingSagaState.PAYMENT_COMPLETED, this::confirmAppointment))
            .flatMap(saga -> executeStep(saga, BookingSagaState.APPOINTMENT_CONFIRMED, this::sendNotifications))
            .flatMap(saga -> completeSaga(saga))
            .onErrorResume(error -> compensate(sagaId, error));
    }
    
    private Mono<SagaState> initializeSaga(String sagaId, BookingCommand command) {
        SagaState saga = SagaState.builder()
            .id(sagaId)
            .type("BOOKING")
            .state(BookingSagaState.INITIATED)
            .data(objectMapper.valueToTree(command))
            .createdAt(Instant.now())
            .build();
            
        return sagaRepo.save(saga);
    }
    
    private Mono<SagaState> executeStep(
            SagaState saga, 
            BookingSagaState expectedState,
            Function<SagaState, Mono<SagaState>> stepFn) {
        
        if (saga.getState() != expectedState) {
            return Mono.just(saga);  // Skip if already past this step
        }
        
        return stepFn.apply(saga)
            .flatMap(updated -> sagaRepo.save(updated))
            .doOnSuccess(s -> log.info("Saga {} completed step: {}", s.getId(), s.getState()));
    }
    
    // Step 1: Reserve the slot
    private Mono<SagaState> reserveSlot(SagaState saga) {
        BookingCommand command = extractCommand(saga);
        
        return appointmentService.reserveSlot(command.getPatientId(), command.toBookingRequest())
            .map(reservation -> saga.toBuilder()
                .state(BookingSagaState.SLOT_RESERVED)
                .addStepData("appointmentId", reservation.getAppointmentId())
                .addStepData("reservedUntil", reservation.getReservedUntil())
                .build());
    }
    
    // Step 2: Initiate payment
    private Mono<SagaState> initiatePayment(SagaState saga) {
        String appointmentId = saga.getStepData("appointmentId");
        BookingCommand command = extractCommand(saga);
        
        PaymentRequest paymentRequest = PaymentRequest.builder()
            .orderType("APPOINTMENT")
            .orderId(appointmentId)
            .userId(command.getPatientId())
            .amount(command.getTotalAmount())
            .currency("INR")
            .description("Appointment booking")
            .build();
        
        return paymentService.initiatePayment(paymentRequest)
            .map(payment -> saga.toBuilder()
                .state(BookingSagaState.PAYMENT_INITIATED)
                .addStepData("paymentId", payment.getPaymentId())
                .addStepData("gatewayOrderId", payment.getGatewayOrderId())
                .build());
    }
    
    // Wait for payment webhook
    private Mono<SagaState> waitForPayment(SagaState saga) {
        // This is handled asynchronously via webhook
        // The webhook handler will update the saga state
        return Mono.just(saga);
    }
    
    // Called by webhook handler when payment completes
    public Mono<SagaState> handlePaymentCompleted(String sagaId, PaymentCompletedEvent event) {
        return sagaRepo.findById(sagaId)
            .flatMap(saga -> {
                if (saga.getState() != BookingSagaState.PAYMENT_INITIATED) {
                    return Mono.just(saga);  // Already processed
                }
                
                return Mono.just(saga.toBuilder()
                    .state(BookingSagaState.PAYMENT_COMPLETED)
                    .addStepData("gatewayPaymentId", event.getGatewayPaymentId())
                    .build());
            })
            .flatMap(saga -> sagaRepo.save(saga))
            .flatMap(saga -> executeStep(saga, BookingSagaState.PAYMENT_COMPLETED, this::confirmAppointment))
            .flatMap(saga -> executeStep(saga, BookingSagaState.APPOINTMENT_CONFIRMED, this::sendNotifications))
            .flatMap(saga -> completeSaga(saga));
    }
    
    // Step 3: Confirm appointment
    private Mono<SagaState> confirmAppointment(SagaState saga) {
        String appointmentId = saga.getStepData("appointmentId");
        String paymentId = saga.getStepData("paymentId");
        
        PaymentConfirmation confirmation = PaymentConfirmation.builder()
            .paymentId(paymentId)
            .status("COMPLETED")
            .build();
        
        return appointmentService.confirmAppointment(
                extractCommand(saga).getPatientId(), 
                appointmentId, 
                confirmation)
            .map(appointment -> saga.toBuilder()
                .state(BookingSagaState.APPOINTMENT_CONFIRMED)
                .build());
    }
    
    // Step 4: Send notifications
    private Mono<SagaState> sendNotifications(SagaState saga) {
        String appointmentId = saga.getStepData("appointmentId");
        
        return notificationService.sendBookingConfirmation(appointmentId)
            .thenReturn(saga.toBuilder()
                .state(BookingSagaState.COMPLETED)
                .build());
    }
    
    private Mono<SagaState> completeSaga(SagaState saga) {
        return sagaRepo.save(saga.toBuilder()
            .state(BookingSagaState.COMPLETED)
            .completedAt(Instant.now())
            .build());
    }
    
    // Compensation (rollback)
    private Mono<BookingResult> compensate(String sagaId, Throwable error) {
        log.error("Saga {} failed, starting compensation", sagaId, error);
        
        return sagaRepo.findById(sagaId)
            .flatMap(saga -> {
                saga = saga.toBuilder()
                    .state(BookingSagaState.COMPENSATING)
                    .errorMessage(error.getMessage())
                    .build();
                    
                return sagaRepo.save(saga);
            })
            .flatMap(saga -> compensateSteps(saga))
            .flatMap(saga -> sagaRepo.save(saga.toBuilder()
                .state(BookingSagaState.COMPENSATION_COMPLETED)
                .build()))
            .map(saga -> BookingResult.failed(error.getMessage()));
    }
    
    private Mono<SagaState> compensateSteps(SagaState saga) {
        List<Mono<Void>> compensations = new ArrayList<>();
        
        // Reverse order compensation
        if (saga.hasStepData("appointmentId")) {
            compensations.add(compensateAppointment(saga));
        }
        if (saga.hasStepData("paymentId")) {
            compensations.add(compensatePayment(saga));
        }
        if (saga.hasStepData("slotReserved")) {
            compensations.add(compensateSlotReservation(saga));
        }
        
        return Flux.concat(compensations)
            .then(Mono.just(saga));
    }
    
    private Mono<Void> compensateSlotReservation(SagaState saga) {
        String appointmentId = saga.getStepData("appointmentId");
        return appointmentService.releaseReservation(appointmentId)
            .doOnSuccess(v -> log.info("Compensated slot reservation for appointment {}", appointmentId));
    }
    
    private Mono<Void> compensatePayment(SagaState saga) {
        String paymentId = saga.getStepData("paymentId");
        // Only refund if payment was actually captured
        return paymentService.findById(paymentId)
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
            .flatMap(p -> paymentService.processRefund(RefundRequest.builder()
                .paymentId(paymentId)
                .amount(p.getAmount())
                .reason("Booking saga compensation")
                .build()))
            .then()
            .doOnSuccess(v -> log.info("Compensated payment {}", paymentId));
    }
    
    private Mono<Void> compensateAppointment(SagaState saga) {
        String appointmentId = saga.getStepData("appointmentId");
        return appointmentService.cancelSystemCancel(appointmentId, "Saga compensation")
            .then()
            .doOnSuccess(v -> log.info("Compensated appointment {}", appointmentId));
    }
}
```
</details>

---

#### Notification Service Tasks - Sprint 5

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B10.1 | Create Notification Service | Project setup | Backend 4 | 4 | P0 | Service runs |
| B10.2 | Notifications Schema | Templates, logs, preferences | Backend 4 | 8 | P0 | Migrations run |
| B10.3 | Email Integration | SendGrid/SES integration | Backend 4 | 8 | P0 | Emails send |
| B10.4 | SMS Integration | Twilio/MSG91 integration | Backend 4 | 8 | P0 | SMS sends |
| B10.5 | Push Notifications | FCM integration | Backend 4 | 8 | P1 | Push works |
| B10.6 | Notification Templates | Handlebars/Thymeleaf templates | Backend 4 | 12 | P0 | Templates render |
| B10.7 | Kafka Consumers | Handle appointment events | Backend 4 | 8 | P0 | Events processed |
| B10.8 | Booking Confirmation | Email + SMS on booking | Backend 4 | 4 | P0 | Confirmation sent |
| B10.9 | Cancellation Notice | Notify on cancellation | Backend 4 | 4 | P0 | Notice sent |
| B10.10 | Reminder Scheduler | Schedule reminders (24h, 1h, 15min) | Backend 4 | 12 | P0 | Reminders scheduled |
| B10.11 | Send Reminders | Job to send due reminders | Backend 4 | 8 | P0 | Reminders sent |
| B10.12 | User Preferences | Opt-in/out for channels | Backend 4 | 8 | P1 | Preferences work |
| B10.13 | Unit & Integration Tests | 80%+ coverage | Backend 4 | 12 | P0 | Tests pass |

**Notification Service Implementation:**

<details>
<summary><strong>B10.6-B10.11 - Notification Templates & Reminders</strong></summary>

```java
// NotificationTemplate.java
public record NotificationTemplate(
    String id,
    String name,
    NotificationType type,
    NotificationChannel channel,
    String subject,           // For email
    String bodyTemplate,      // Handlebars template
    String locale,
    boolean isActive
) {}

// NotificationService.java
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final EmailService emailService;
    private final SmsService smsService;
    private final PushService pushService;
    private final NotificationTemplateRepository templateRepo;
    private final NotificationLogRepository logRepo;
    private final UserPreferencesRepository preferencesRepo;
    private final ScheduledNotificationRepository scheduledRepo;
    private final Handlebars handlebars;
    
    public Mono<Void> sendBookingConfirmation(Appointment appointment) {
        return Flux.merge(
            sendNotification(
                appointment.getPatientId(),
                NotificationType.BOOKING_CONFIRMED,
                NotificationChannel.EMAIL,
                buildBookingContext(appointment)
            ),
            sendNotification(
                appointment.getPatientId(),
                NotificationType.BOOKING_CONFIRMED,
                NotificationChannel.SMS,
                buildBookingContext(appointment)
            ),
            sendNotification(
                appointment.getDoctorId(),
                NotificationType.NEW_APPOINTMENT,
                NotificationChannel.PUSH,
                buildBookingContext(appointment)
            )
        )
        .then()
        .doOnSuccess(v -> scheduleReminders(appointment));
    }
    
    public Mono<Void> sendNotification(
            String userId,
            NotificationType type,
            NotificationChannel channel,
            Map<String, Object> context) {
        
        return checkUserPreference(userId, channel)
            .filter(enabled -> enabled)
            .flatMap(enabled -> templateRepo.findByTypeAndChannel(type, channel))
            .flatMap(template -> renderTemplate(template, context))
            .flatMap(rendered -> sendViaChannel(userId, channel, rendered))
            .flatMap(result -> logNotification(userId, type, channel, result))
            .then();
    }
    
    private Mono<RenderedNotification> renderTemplate(
            NotificationTemplate template, 
            Map<String, Object> context) {
        
        return Mono.fromCallable(() -> {
            Template compiled = handlebars.compileInline(template.bodyTemplate());
            String body = compiled.apply(context);
            
            String subject = null;
            if (template.subject() != null) {
                Template subjectTemplate = handlebars.compileInline(template.subject());
                subject = subjectTemplate.apply(context);
            }
            
            return new RenderedNotification(subject, body);
        });
    }
    
    private Mono<NotificationResult> sendViaChannel(
            String userId, 
            NotificationChannel channel, 
            RenderedNotification notification) {
        
        return getUserContactInfo(userId, channel)
            .flatMap(contactInfo -> {
                switch (channel) {
                    case EMAIL:
                        return emailService.send(
                            contactInfo, 
                            notification.subject(), 
                            notification.body());
                    case SMS:
                        return smsService.send(contactInfo, notification.body());
                    case PUSH:
                        return pushService.send(contactInfo, notification.subject(), notification.body());
                    default:
                        return Mono.error(new IllegalArgumentException("Unknown channel"));
                }
            });
    }
    
    // Schedule reminders for appointment
    private void scheduleReminders(Appointment appointment) {
        Instant appointmentTime = appointment.getScheduledAt();
        
        List<ScheduledNotification> reminders = List.of(
            // 24 hours before
            ScheduledNotification.builder()
                .appointmentId(appointment.getId())
                .userId(appointment.getPatientId())
                .type(NotificationType.APPOINTMENT_REMINDER)
                .channel(NotificationChannel.EMAIL)
                .scheduledFor(appointmentTime.minus(Duration.ofHours(24)))
                .build(),
            // 1 hour before
            ScheduledNotification.builder()
                .appointmentId(appointment.getId())
                .userId(appointment.getPatientId())
                .type(NotificationType.APPOINTMENT_REMINDER)
                .channel(NotificationChannel.SMS)
                .scheduledFor(appointmentTime.minus(Duration.ofHours(1)))
                .build(),
            // 15 minutes before
            ScheduledNotification.builder()
                .appointmentId(appointment.getId())
                .userId(appointment.getPatientId())
                .type(NotificationType.APPOINTMENT_REMINDER)
                .channel(NotificationChannel.PUSH)
                .scheduledFor(appointmentTime.minus(Duration.ofMinutes(15)))
                .build(),
            // Doctor reminder - 30 minutes before
            ScheduledNotification.builder()
                .appointmentId(appointment.getId())
                .userId(appointment.getDoctorId())
                .type(NotificationType.APPOINTMENT_REMINDER)
                .channel(NotificationChannel.PUSH)
                .scheduledFor(appointmentTime.minus(Duration.ofMinutes(30)))
                .build()
        );
        
        scheduledRepo.saveAll(reminders).subscribe();
    }
    
    private Map<String, Object> buildBookingContext(Appointment appointment) {
        return Map.of(
            "patientName", appointment.getPatientName(),
            "doctorName", appointment.getDoctorName(),
            "appointmentDate", formatDate(appointment.getScheduledAt()),
            "appointmentTime", formatTime(appointment.getScheduledAt()),
            "consultationType", appointment.getConsultationType().getDisplayName(),
            "clinicName", appointment.getClinicName() != null ? appointment.getClinicName() : "Video Consultation",
            "appointmentId", appointment.getId(),
            "amount", formatCurrency(appointment.getTotalAmount())
        );
    }
}

// ReminderScheduler.java
@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {
    
    private final ScheduledNotificationRepository scheduledRepo;
    private final NotificationService notificationService;
    private final AppointmentClient appointmentClient;
    
    @Scheduled(fixedRate = 60000)  // Every minute
    public void processScheduledNotifications() {
        Instant now = Instant.now();
        Instant windowEnd = now.plus(Duration.ofMinutes(1));
        
        scheduledRepo.findDueNotifications(now, windowEnd)
            .filter(n -> n.getStatus() == ScheduledStatus.PENDING)
            .flatMap(this::sendScheduledNotification)
            .subscribe(
                n -> log.info("Sent scheduled notification: {}", n.getId()),
                error -> log.error("Error sending notification", error)
            );
    }
    
    private Mono<ScheduledNotification> sendScheduledNotification(ScheduledNotification scheduled) {
        return appointmentClient.getAppointment(scheduled.getAppointmentId())
            .filter(apt -> apt.getStatus().isActive())  // Don't send for cancelled
            .flatMap(appointment -> {
                Map<String, Object> context = buildReminderContext(appointment, scheduled);
                
                return notificationService.sendNotification(
                    scheduled.getUserId(),
                    scheduled.getType(),
                    scheduled.getChannel(),
                    context
                );
            })
            .then(scheduledRepo.save(scheduled.toBuilder()
                .status(ScheduledStatus.SENT)
                .sentAt(Instant.now())
                .build()))
            .onErrorResume(error -> {
                log.error("Failed to send scheduled notification {}", scheduled.getId(), error);
                return scheduledRepo.save(scheduled.toBuilder()
                    .status(ScheduledStatus.FAILED)
                    .errorMessage(error.getMessage())
                    .build());
            });
    }
}
```
</details>

<details>
<summary><strong>Notification Templates (Examples)</strong></summary>

```handlebars
{{!-- booking_confirmed_email.hbs --}}
<!DOCTYPE html>
<html>
<head>
    <style>
        .container { max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif; }
        .header { background: #0891B2; color: white; padding: 20px; text-align: center; }
        .content { padding: 20px; }
        .details { background: #f5f5f5; padding: 15px; border-radius: 8px; margin: 20px 0; }
        .button { background: #0891B2; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; display: inline-block; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Appointment Confirmed! ✓</h1>
        </div>
        <div class="content">
            <p>Dear {{patientName}},</p>
            <p>Your appointment has been successfully booked.</p>
            
            <div class="details">
                <h3>Appointment Details</h3>
                <p><strong>Doctor:</strong> Dr. {{doctorName}}</p>
                <p><strong>Date:</strong> {{appointmentDate}}</p>
                <p><strong>Time:</strong> {{appointmentTime}}</p>
                <p><strong>Type:</strong> {{consultationType}}</p>
                {{#if clinicName}}
                <p><strong>Location:</strong> {{clinicName}}</p>
                {{/if}}
                <p><strong>Amount Paid:</strong> {{amount}}</p>
            </div>
            
            {{#if isVideoConsultation}}
            <p>You will receive a link to join the video consultation 15 minutes before your appointment.</p>
            {{/if}}
            
            <a href="{{appointmentUrl}}" class="button">View Appointment</a>
            
            <p style="margin-top: 20px; color: #666;">
                Need to reschedule? You can modify your appointment up to 2 hours before the scheduled time.
            </p>
        </div>
    </div>
</body>
</html>

{{!-- booking_confirmed_sms.hbs --}}
Your appointment with Dr. {{doctorName}} is confirmed for {{appointmentDate}} at {{appointmentTime}}. 
{{#if isVideoConsultation}}Video call link will be sent before appointment.{{/if}}
Booking ID: {{appointmentId}}

{{!-- appointment_reminder_sms.hbs --}}
Reminder: Your appointment with Dr. {{doctorName}} is in {{timeUntil}}. 
{{#if isVideoConsultation}}Join here: {{joinUrl}}{{else}}Address: {{clinicAddress}}{{/if}}
```
</details>

---

#### Frontend Tasks - Sprint 5

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| F5.1 | Payment Integration | Razorpay SDK integration | Frontend 1 | 16 | P0 | Payment works |
| F5.2 | Payment Flow UI | Loading, success, failure states | Frontend 1 | 12 | P0 | All states handled |
| F5.3 | Payment Success Page | Confirmation with details | Frontend 1 | 8 | P0 | Success page works |
| F5.4 | Payment Failure Page | Retry option, error display | Frontend 1 | 8 | P0 | Failure handled |
| F5.5 | Cancellation Modal | Cancel with reason | Frontend 1 | 8 | P0 | Cancellation works |
| F5.6 | Reschedule Flow | Select new slot | Frontend 1 | 12 | P1 | Reschedule works |
| F5.7 | Today's Schedule (Doctor) | Day view with timeline | Frontend 2 | 16 | P0 | Schedule displays |
| F5.8 | Notification Badge | Real-time notification indicator | Frontend 1 | 8 | P1 | Badge updates |

**Frontend Implementation:**

<details>
<summary><strong>F5.1 & F5.2 - Razorpay Payment Integration</strong></summary>

```typescript
// hooks/usePayment.ts
import { useState, useCallback } from 'react';
import { paymentApi } from '@/lib/api/payment';
import { useToast } from '@/hooks/use-toast';

declare global {
  interface Window {
    Razorpay: any;
  }
}

interface PaymentOptions {
  appointmentId: string;
  amount: number;
  currency: string;
  doctorName: string;
  patientEmail: string;
  patientPhone: string;
}

interface PaymentResult {
  success: boolean;
  paymentId?: string;
  error?: string;
}

export function usePayment() {
  const [isLoading, setIsLoading] = useState(false);
  const { toast } = useToast();
  
  const loadRazorpayScript = useCallback(() => {
    return new Promise<void>((resolve, reject) => {
      if (window.Razorpay) {
        resolve();
        return;
      }
      
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.onload = () => resolve();
      script.onerror = () => reject(new Error('Failed to load Razorpay'));
      document.body.appendChild(script);
    });
  }, []);
  
  const initiatePayment = useCallback(async (options: PaymentOptions): Promise<PaymentResult> => {
    setIsLoading(true);
    
    try {
      // Load Razorpay script
      await loadRazorpayScript();
      
      // Create payment order
      const paymentOrder = await paymentApi.initiatePayment({
        orderType: 'APPOINTMENT',
        orderId: options.appointmentId,
        amount: options.amount,
        currency: options.currency,
        description: `Appointment with Dr. ${options.doctorName}`,
      });
      
      // Open Razorpay checkout
      return new Promise((resolve) => {
        const razorpay = new window.Razorpay({
          key: paymentOrder.razorpayKeyId,
          amount: options.amount * 100, // In paise
          currency: options.currency,
          name: 'HealthCare Platform',
          description: `Appointment with Dr. ${options.doctorName}`,
          order_id: paymentOrder.gatewayOrderId,
          prefill: {
            email: options.patientEmail,
            contact: options.patientPhone,
          },
          theme: {
            color: '#0891B2',
          },
          handler: async (response: any) => {
            try {
              // Verify payment on backend
              const verification = await paymentApi.verifyPayment({
                gatewayOrderId: response.razorpay_order_id,
                gatewayPaymentId: response.razorpay_payment_id,
                gatewaySignature: response.razorpay_signature,
              });
              
              if (verification.valid) {
                resolve({
                  success: true,
                  paymentId: response.razorpay_payment_id,
                });
              } else {
                resolve({
                  success: false,
                  error: 'Payment verification failed',
                });
              }
            } catch (error) {
              resolve({
                success: false,
                error: 'Payment verification failed',
              });
            }
          },
          modal: {
            ondismiss: () => {
              resolve({
                success: false,
                error: 'Payment cancelled by user',
              });
            },
          },
        });
        
        razorpay.on('payment.failed', (response: any) => {
          resolve({
            success: false,
            error: response.error.description || 'Payment failed',
          });
        });
        
        razorpay.open();
      });
    } catch (error: any) {
      toast({
        title: 'Payment Error',
        description: error.message,
        variant: 'destructive',
      });
      return {
        success: false,
        error: error.message,
      };
    } finally {
      setIsLoading(false);
    }
  }, [loadRazorpayScript, toast]);
  
  return {
    initiatePayment,
    isLoading,
  };
}

// components/booking/PaymentFlow.tsx
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { usePayment } from '@/hooks/usePayment';
import { Card, CardHeader, CardContent, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Loader2, CheckCircle, XCircle, CreditCard } from 'lucide-react';
import { BookingDetails, SlotDto } from '@/types/booking';
import { formatCurrency, formatDateTime } from '@/lib/utils';

interface PaymentFlowProps {
  booking: BookingDetails;
  slot: SlotDto;
  onSuccess: () => void;
  onCancel: () => void;
}

type PaymentState = 'summary' | 'processing' | 'success' | 'failed';

export function PaymentFlow({ booking, slot, onSuccess, onCancel }: PaymentFlowProps) {
  const [state, setState] = useState<PaymentState>('summary');
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();
  const { initiatePayment, isLoading } = usePayment();
  
  const handlePayment = async () => {
    setState('processing');
    
    const result = await initiatePayment({
      appointmentId: booking.appointmentId,
      amount: booking.totalAmount,
      currency: 'INR',
      doctorName: booking.doctorName,
      patientEmail: booking.patientEmail,
      patientPhone: booking.patientPhone,
    });
    
    if (result.success) {
      setState('success');
      setTimeout(() => {
        onSuccess();
        router.push(`/appointments/${booking.appointmentId}?payment=success`);
      }, 2000);
    } else {
      setState('failed');
      setError(result.error || 'Payment failed');
    }
  };
  
  const handleRetry = () => {
    setState('summary');
    setError(null);
  };
  
  return (
    <Card className="max-w-md mx-auto">
      {state === 'summary' && (
        <>
          <CardHeader>
            <h2 className="text-xl font-semibold">Complete Payment</h2>
            <p className="text-gray-500">Review and pay to confirm your appointment</p>
          </CardHeader>
          
          <CardContent className="space-y-4">
            {/* Booking Summary */}
            <div className="bg-gray-50 p-4 rounded-lg space-y-2">
              <div className="flex justify-between">
                <span className="text-gray-600">Doctor</span>
                <span className="font-medium">Dr. {booking.doctorName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Date & Time</span>
                <span className="font-medium">
                  {formatDateTime(booking.scheduledAt)}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Type</span>
                <span className="font-medium">{booking.consultationType}</span>
              </div>
            </div>
            
            {/* Price Breakdown */}
            <div className="space-y-2">
              <div className="flex justify-between text-gray-600">
                <span>Consultation Fee</span>
                <span>{formatCurrency(booking.consultationFee)}</span>
              </div>
              <div className="flex justify-between text-gray-600">
                <span>Platform Fee</span>
                <span>{formatCurrency(booking.platformFee)}</span>
              </div>
              {booking.discount > 0 && (
                <div className="flex justify-between text-green-600">
                  <span>Discount</span>
                  <span>-{formatCurrency(booking.discount)}</span>
                </div>
              )}
              <hr />
              <div className="flex justify-between font-semibold text-lg">
                <span>Total</span>
                <span>{formatCurrency(booking.totalAmount)}</span>
              </div>
            </div>
            
            {/* Reservation Timer */}
            <div className="text-center text-sm text-orange-600 bg-orange-50 p-2 rounded">
              ⏱️ Slot reserved for {booking.expiresInMinutes} minutes
            </div>
          </CardContent>
          
          <CardFooter className="flex gap-3">
            <Button variant="outline" onClick={onCancel} className="flex-1">
              Cancel
            </Button>
            <Button onClick={handlePayment} className="flex-1">
              <CreditCard className="w-4 h-4 mr-2" />
              Pay {formatCurrency(booking.totalAmount)}
            </Button>
          </CardFooter>
        </>
      )}
      
      {state === 'processing' && (
        <CardContent className="py-12 text-center">
          <Loader2 className="w-12 h-12 mx-auto mb-4 animate-spin text-primary-500" />
          <h3 className="text-lg font-medium">Processing Payment</h3>
          <p className="text-gray-500 mt-2">Please wait while we process your payment...</p>
        </CardContent>
      )}
      
      {state === 'success' && (
        <CardContent className="py-12 text-center">
          <CheckCircle className="w-16 h-16 mx-auto mb-4 text-green-500" />
          <h3 className="text-xl font-semibold text-green-700">Payment Successful!</h3>
          <p className="text-gray-500 mt-2">Your appointment has been confirmed.</p>
          <p className="text-gray-400 mt-1">Redirecting to appointment details...</p>
        </CardContent>
      )}
      
      {state === 'failed' && (
        <CardContent className="py-12 text-center">
          <XCircle className="w-16 h-16 mx-auto mb-4 text-red-500" />
          <h3 className="text-xl font-semibold text-red-700">Payment Failed</h3>
          <p className="text-gray-500 mt-2">{error}</p>
          <div className="mt-6 flex gap-3 justify-center">
            <Button variant="outline" onClick={onCancel}>
              Cancel Booking
            </Button>
            <Button onClick={handleRetry}>
              Try Again
            </Button>
          </div>
        </CardContent>
      )}
    </Card>
  );
}
```
</details>

---

### Sprint 5 Deliverables Checklist

- [ ] **Payment Service**
  - [ ] Razorpay integration complete
  - [ ] Payment initiation and verification
  - [ ] Webhook handling
  - [ ] Refund processing
  - [ ] Payment history endpoint

- [ ] **Booking Saga**
  - [ ] Full booking flow with saga
  - [ ] Compensation on failure
  - [ ] Idempotent event handling
  - [ ] State persistence

- [ ] **Notification Service**
  - [ ] Email notifications (SendGrid/SES)
  - [ ] SMS notifications (Twilio/MSG91)
  - [ ] Push notifications (FCM)
  - [ ] Booking confirmations sending
  - [ ] Reminders scheduled and sent

- [ ] **Frontend**
  - [ ] Razorpay checkout integration
  - [ ] Payment success/failure pages
  - [ ] Cancellation with refund
  - [ ] Doctor's daily schedule view

---

## Phase 2 Completion Criteria

### Functional Requirements ✓

| Requirement | Acceptance Criteria | Status |
|-------------|---------------------|--------|
| Doctor Availability | Doctor can set weekly schedule | ⬜ |
| Slot Blocking | Doctor can block time for leave | ⬜ |
| View Available Slots | Patient sees available slots on profile | ⬜ |
| Book Appointment | Patient can reserve slot and pay | ⬜ |
| Payment Processing | Razorpay payment completes | ⬜ |
| Booking Confirmation | Email + SMS sent on booking | ⬜ |
| Appointment Reminders | Reminders sent 24h, 1h before | ⬜ |
| Cancel Appointment | Patient can cancel with refund | ⬜ |
| Doctor Appointments | Doctor sees daily schedule | ⬜ |

### Non-Functional Requirements ✓

| Requirement | Criteria | Status |
|-------------|----------|--------|
| Booking Transaction | Saga ensures consistency | ⬜ |
| Payment Security | PCI-DSS compliant integration | ⬜ |
| Notification Delivery | 95%+ delivery rate | ⬜ |
| Reminder Accuracy | Reminders sent within 1 min of scheduled time | ⬜ |
| Payment SLA | < 5 sec for payment initiation | ⬜ |

---

## Phase 2 Testing Scenarios

### Critical Path Testing

```markdown
## Test Scenario 1: Happy Path Booking

1. Patient searches for doctor
2. Selects available slot
3. Reviews booking summary
4. Completes Razorpay payment
5. Receives confirmation email + SMS
6. Appointment appears in "My Appointments"
7. Doctor sees appointment in dashboard
8. Patient receives reminders (24h, 1h before)

Expected: All steps complete successfully

## Test Scenario 2: Payment Failure & Retry

1. Patient reserves slot
2. Payment fails (card declined)
3. Slot remains reserved (10 min)
4. Patient retries with different card
5. Payment succeeds
6. Booking confirmed

Expected: Slot not released during retry window

## Test Scenario 3: Saga Compensation

1. Patient reserves slot
2. Payment succeeds
3. Confirmation fails (appointment service down)
4. Saga triggers compensation
5. Payment refunded
6. Slot released
7. Patient notified of failure

Expected: All resources rolled back

## Test Scenario 4: Cancellation with Refund

1. Patient has confirmed appointment
2. Cancels 6 hours before (within policy)
3. Full refund initiated
4. Slot becomes available again
5. Both parties notified

Expected: Refund processed, slot available

## Test Scenario 5: Expired Reservation

1. Patient reserves slot
2. Does not complete payment in 10 min
3. System auto-cancels
4. Slot becomes available
5. No refund needed (no payment)

Expected: Slot auto-released
```

---

## Phase 2 Sign-off

| Role | Name | Sign-off Date | Signature |
|------|------|---------------|-----------|
| Tech Lead | | | |
| Product Manager | | | |
| QA Lead | | | |
| Security Lead | | | |

---

## Appendix

### A. API Contracts

- `appointment-service-api.yaml`
- `payment-service-api.yaml`
- `notification-service-api.yaml`

### B. Event Catalog - Phase 2

| Event | Publisher | Consumers |
|-------|-----------|-----------|
| `slot.reserved` | Appointment Svc | Saga Orchestrator |
| `appointment.confirmed` | Appointment Svc | Notification, Analytics, Search |
| `appointment.cancelled` | Appointment Svc | Payment (refund), Notification |
| `payment.completed` | Payment Svc | Saga, Appointment, Notification |
| `payment.failed` | Payment Svc | Saga, Notification |
| `refund.processed` | Payment Svc | Notification |
| `reminder.scheduled` | Notification Svc | Scheduler |
| `notification.sent` | Notification Svc | Analytics |

### C. Saga State Diagram

```
                    ┌──────────────┐
                    │  INITIATED   │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │ SLOT_RESERVED│
                    └──────┬───────┘
                           │
               ┌───────────▼───────────┐
               │  PAYMENT_INITIATED    │
               └───────────┬───────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
   ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐
   │   PAYMENT   │  │   PAYMENT   │  │  TIMEOUT    │
   │  COMPLETED  │  │   FAILED    │  │             │
   └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
          │                │                │
   ┌──────▼──────┐         └────────┬───────┘
   │ APPOINTMENT │                  │
   │  CONFIRMED  │           ┌──────▼──────┐
   └──────┬──────┘           │ COMPENSATING│
          │                  └──────┬──────┘
   ┌──────▼──────┐                  │
   │  COMPLETED  │           ┌──────▼──────┐
   └─────────────┘           │   FAILED    │
                             └─────────────┘
```

---

*Document Version: 1.0*  
*Last Updated: January 30, 2026*
