package com.healthapp.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthapp.order.domain.*;
import com.healthapp.order.domain.enums.*;
import com.healthapp.order.dto.*;
import com.healthapp.order.event.LabBookingEventPublisher;
import com.healthapp.order.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for lab test booking management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabBookingService {

    private final LabBookingRepository bookingRepository;
    private final LabTestRepository testRepository;
    private final TestPackageRepository packageRepository;
    private final TestCategoryRepository categoryRepository;
    private final CollectionSlotRepository slotRepository;
    private final PartnerRepository partnerRepository;
    private final DeliveryAddressRepository addressRepository;
    private final LabBookingEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${lab.home-collection-fee:50.00}")
    private BigDecimal homeCollectionFee;

    private static final DateTimeFormatter BOOKING_NUMBER_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Get all test categories.
     */
    public Flux<TestCategory> getCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * Search lab tests.
     */
    public Flux<LabTestResponse> searchTests(String query, int limit) {
        return testRepository.searchTests(query, limit)
                .map(this::toLabTestResponse);
    }

    /**
     * Search lab tests with pagination.
     */
    public Flux<LabTestResponse> searchLabTests(String keyword, int page, int size) {
        return testRepository.searchTests(keyword, size)
                .skip((long) page * size)
                .take(size)
                .map(this::toLabTestResponse);
    }

    /**
     * Get lab test by ID.
     */
    public Mono<LabTestResponse> getLabTest(UUID testId) {
        return testRepository.findById(testId)
                .map(this::toLabTestResponse);
    }

    /**
     * Get popular tests.
     */
    public Flux<LabTestResponse> getPopularTests() {
        return testRepository.findByIsPopularTrueAndIsActiveTrue()
                .map(this::toLabTestResponse);
    }

    /**
     * Get popular tests with limit.
     */
    public Flux<LabTestResponse> getPopularTests(int limit) {
        return testRepository.findByIsPopularTrueAndIsActiveTrue()
                .take(limit)
                .map(this::toLabTestResponse);
    }

    /**
     * Get tests by category.
     */
    public Flux<LabTestResponse> getTestsByCategory(UUID categoryId) {
        return testRepository.findByCategoryIdAndIsActiveTrue(categoryId)
                .map(this::toLabTestResponse);
    }

    /**
     * Get tests by category with pagination.
     */
    public Flux<LabTestResponse> getTestsByCategory(UUID categoryId, int page, int size) {
        return testRepository.findByCategoryIdAndIsActiveTrue(categoryId)
                .skip((long) page * size)
                .take(size)
                .map(this::toLabTestResponse);
    }

    /**
     * Get test by ID.
     */
    public Mono<LabTestResponse> getTest(UUID testId) {
        return testRepository.findById(testId)
                .map(this::toLabTestResponse);
    }

    /**
     * Get all test packages.
     */
    public Flux<TestPackageResponse> getPackages() {
        return packageRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .flatMap(this::toTestPackageResponse);
    }

    /**
     * Get all test packages with pagination.
     */
    public Flux<TestPackageResponse> getTestPackages(int page, int size) {
        return packageRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .skip((long) page * size)
                .take(size)
                .flatMap(this::toTestPackageResponse);
    }

    /**
     * Get test package by ID.
     */
    public Mono<TestPackageResponse> getTestPackage(UUID packageId) {
        return packageRepository.findById(packageId)
                .flatMap(this::toTestPackageResponse);
    }

    /**
     * Get popular packages.
     */
    public Flux<TestPackageResponse> getPopularPackages() {
        return packageRepository.findByIsPopularTrueAndIsActiveTrue()
                .flatMap(this::toTestPackageResponse);
    }

    /**
     * Get popular packages with limit.
     */
    public Flux<TestPackageResponse> getPopularPackages(int limit) {
        return packageRepository.findByIsPopularTrueAndIsActiveTrue()
                .take(limit)
                .flatMap(this::toTestPackageResponse);
    }

    /**
     * Get available collection slots (overloaded for controller).
     */
    public Flux<AvailableSlotResponse> getAvailableSlots(UUID labPartnerId, LocalDate date, String pincode) {
        return getAvailableSlots(pincode, date, date.plusDays(1));
    }

    /**
     * Get available collection slots.
     */
    public Flux<AvailableSlotResponse> getAvailableSlots(String pincode, LocalDate startDate, LocalDate endDate) {
        return slotRepository.findAvailableSlotsForPincode(startDate, endDate, pincode)
                .flatMap(slot -> partnerRepository.findById(slot.getLabPartnerId())
                        .map(partner -> toAvailableSlotResponse(slot, partner)));
    }

    /**
     * Create a lab booking.
     */
    @Transactional
    public Mono<LabBookingResponse> createBooking(UUID userId, CreateLabBookingRequest request) {
        log.info("Creating lab booking for user: {}", userId);

        return Mono.zip(
                partnerRepository.findById(request.getLabPartnerId()),
                getTestsForBooking(request.getTestIds()),
                request.getCollectionAddressId() != null 
                    ? addressRepository.findById(request.getCollectionAddressId()) 
                    : Mono.empty()
        ).flatMap(tuple -> {
            Partner lab = tuple.getT1();
            List<LabTest> tests = tuple.getT2();
            DeliveryAddress address = tuple.size() > 2 ? tuple.getT3() : null;

            // Calculate pricing
            BigDecimal subtotal = tests.stream()
                    .map(LabTest::getBasePrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal collectionFee = request.getBookingType() == BookingType.HOME_COLLECTION 
                    ? homeCollectionFee : BigDecimal.ZERO;
            BigDecimal total = subtotal.add(collectionFee);

            // Create booking
            LabBooking booking = LabBooking.builder()
                    .bookingNumber(generateBookingNumber())
                    .userId(userId)
                    .patientName(request.getPatientName())
                    .patientAge(request.getPatientAge())
                    .patientGender(request.getPatientGender())
                    .patientPhone(request.getPatientPhone())
                    .bookingType(request.getBookingType())
                    .labPartnerId(request.getLabPartnerId())
                    .labPartnerName(lab.getBusinessName())
                    .collectionAddressId(request.getCollectionAddressId())
                    .collectionAddressSnapshot(address != null ? serializeAddress(address) : null)
                    .scheduledDate(request.getScheduledDate())
                    .scheduledSlot(request.getScheduledSlot())
                    .tests(serializeTests(tests))
                    .packageId(request.getPackageId())
                    .subtotal(subtotal)
                    .discountAmount(BigDecimal.ZERO)
                    .homeCollectionFee(collectionFee)
                    .totalAmount(total)
                    .paymentStatus(PaymentStatus.PENDING)
                    .status(LabBookingStatus.PENDING)
                    .notes(request.getNotes())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            // Reserve slot if provided
            Mono<Void> reserveSlot = Mono.empty();
            if (request.getSlotId() != null) {
                reserveSlot = slotRepository.incrementBookingCount(request.getSlotId()).then();
            }

            return reserveSlot
                    .then(bookingRepository.save(booking));
        })
        .flatMap(booking -> getBookingResponse(booking.getId()))
        .doOnSuccess(response -> eventPublisher.publishBookingCreated(response));
    }

    /**
     * Get booking by ID.
     */
    public Mono<LabBookingResponse> getBooking(UUID bookingId) {
        return getBookingResponse(bookingId);
    }

    /**
     * Get user's bookings.
     */
    public Flux<LabBookingResponse> getUserBookings(UUID userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .flatMap(booking -> getBookingResponse(booking.getId()));
    }

    /**
     * Get user's bookings with pagination.
     */
    public Flux<LabBookingResponse> getUserBookings(UUID userId, int page, int size) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .skip((long) page * size)
                .take(size)
                .flatMap(booking -> getBookingResponse(booking.getId()));
    }

    /**
     * Get user's bookings by status with pagination.
     */
    public Flux<LabBookingResponse> getUserBookingsByStatus(UUID userId, LabBookingStatus status, int page, int size) {
        return bookingRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status)
                .skip((long) page * size)
                .take(size)
                .flatMap(booking -> getBookingResponse(booking.getId()));
    }

    /**
     * Get booking by booking number.
     */
    public Mono<LabBookingResponse> getBookingByNumber(String bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber)
                .flatMap(booking -> getBookingResponse(booking.getId()));
    }

    /**
     * Get partner's bookings with pagination.
     */
    public Flux<LabBookingResponse> getPartnerBookings(UUID partnerId, LabBookingStatus status, int page, int size) {
        if (status != null) {
            return bookingRepository.findByLabPartnerIdAndStatusOrderByCreatedAtDesc(partnerId, status)
                    .skip((long) page * size)
                    .take(size)
                    .flatMap(booking -> getBookingResponse(booking.getId()));
        }
        return bookingRepository.findByLabPartnerIdOrderByCreatedAtDesc(partnerId)
                .skip((long) page * size)
                .take(size)
                .flatMap(booking -> getBookingResponse(booking.getId()));
    }

    /**
     * Confirm payment for booking.
     */
    @Transactional
    public Mono<LabBookingResponse> confirmPayment(UUID bookingId, UUID paymentId) {
        log.info("Confirming payment for booking: {}", bookingId);

        return bookingRepository.findById(bookingId)
                .flatMap(booking -> {
                    booking.setPaymentId(paymentId);
                    booking.setPaymentStatus(PaymentStatus.COMPLETED);
                    booking.setPaidAt(Instant.now());
                    booking.setStatus(LabBookingStatus.CONFIRMED);
                    booking.setUpdatedAt(Instant.now());
                    return bookingRepository.save(booking);
                })
                .flatMap(booking -> getBookingResponse(booking.getId()))
                .doOnSuccess(response -> eventPublisher.publishBookingConfirmed(response));
    }

    /**
     * Confirm payment for booking with paymentId and transactionId strings.
     */
    @Transactional
    public Mono<LabBookingResponse> confirmPayment(UUID bookingId, String paymentId, String transactionId) {
        log.info("Confirming payment for booking: {}, payment: {}, transaction: {}", bookingId, paymentId, transactionId);
        return confirmPayment(bookingId, UUID.fromString(paymentId));
    }

    /**
     * Cancel booking.
     */
    @Transactional
    public Mono<LabBookingResponse> cancelBooking(UUID bookingId, UUID userId, String reason) {
        log.info("Cancelling booking: {} for user: {}", bookingId, userId);

        return bookingRepository.findById(bookingId)
                .filter(booking -> booking.getUserId().equals(userId))
                .filter(booking -> canCancel(booking.getStatus()))
                .switchIfEmpty(Mono.error(new IllegalStateException("Booking cannot be cancelled")))
                .flatMap(booking -> {
                    booking.setStatus(LabBookingStatus.CANCELLED);
                    booking.setCancelledAt(Instant.now());
                    booking.setCancellationReason(reason);
                    booking.setUpdatedAt(Instant.now());
                    return bookingRepository.save(booking);
                })
                .flatMap(booking -> getBookingResponse(booking.getId()))
                .doOnSuccess(response -> eventPublisher.publishBookingCancelled(response));
    }

    /**
     * Cancel booking without userId check.
     */
    @Transactional
    public Mono<LabBookingResponse> cancelBooking(UUID bookingId, String reason) {
        log.info("Cancelling booking: {}", bookingId);

        return bookingRepository.findById(bookingId)
                .filter(booking -> canCancel(booking.getStatus()))
                .switchIfEmpty(Mono.error(new IllegalStateException("Booking cannot be cancelled")))
                .flatMap(booking -> {
                    booking.setStatus(LabBookingStatus.CANCELLED);
                    booking.setCancelledAt(Instant.now());
                    booking.setCancellationReason(reason);
                    booking.setUpdatedAt(Instant.now());
                    return bookingRepository.save(booking);
                })
                .flatMap(booking -> getBookingResponse(booking.getId()))
                .doOnSuccess(response -> eventPublisher.publishBookingCancelled(response));
    }

    /**
     * Reschedule booking.
     */
    @Transactional
    public Mono<LabBookingResponse> rescheduleBooking(UUID bookingId, LocalDate newDate, UUID newSlotId) {
        log.info("Rescheduling booking: {} to date: {}", bookingId, newDate);

        return bookingRepository.findById(bookingId)
                .filter(booking -> canReschedule(booking.getStatus()))
                .switchIfEmpty(Mono.error(new IllegalStateException("Booking cannot be rescheduled")))
                .flatMap(booking -> {
                    booking.setScheduledDate(newDate);
                    booking.setUpdatedAt(Instant.now());
                    return bookingRepository.save(booking);
                })
                .flatMap(booking -> getBookingResponse(booking.getId()));
    }

    /**
     * Assign phlebotomist to booking.
     */
    @Transactional
    public Mono<LabBookingResponse> assignPhlebotomist(UUID bookingId, UUID phlebotomistId) {
        log.info("Assigning phlebotomist: {} to booking: {}", phlebotomistId, bookingId);

        return bookingRepository.findById(bookingId)
                .flatMap(booking -> {
                    booking.setPhlebotomistId(phlebotomistId);
                    booking.setUpdatedAt(Instant.now());
                    return bookingRepository.save(booking);
                })
                .flatMap(booking -> getBookingResponse(booking.getId()));
    }

    /**
     * Upload lab report with FilePart.
     */
    @Transactional
    public Mono<LabBookingResponse> uploadReport(UUID bookingId, org.springframework.http.codec.multipart.FilePart file) {
        log.info("Uploading report for booking: {}", bookingId);
        // TODO: Upload file to storage and get URL
        String reportUrl = "/reports/" + bookingId + "/" + file.filename();
        UUID reportDocumentId = UUID.randomUUID();
        return uploadReport(bookingId, reportUrl, reportDocumentId);
    }

    private boolean canReschedule(LabBookingStatus status) {
        return status == LabBookingStatus.PENDING 
            || status == LabBookingStatus.CONFIRMED;
    }

    /**
     * Update booking status (for lab partner).
     */
    @Transactional
    public Mono<LabBookingResponse> updateBookingStatus(UUID bookingId, LabBookingStatus newStatus, String notes) {
        log.info("Updating booking status: {} to {}", bookingId, newStatus);

        return bookingRepository.findById(bookingId)
                .flatMap(booking -> {
                    booking.setStatus(newStatus);
                    booking.setUpdatedAt(Instant.now());

                    if (newStatus == LabBookingStatus.SAMPLE_COLLECTED) {
                        booking.setSampleCollectedAt(Instant.now());
                    }
                    if (newStatus == LabBookingStatus.REPORT_READY) {
                        booking.setReportReadyAt(Instant.now());
                    }

                    return bookingRepository.save(booking);
                })
                .flatMap(booking -> getBookingResponse(booking.getId()))
                .doOnSuccess(response -> eventPublisher.publishBookingStatusUpdated(response));
    }

    /**
     * Upload lab report.
     */
    @Transactional
    public Mono<LabBookingResponse> uploadReport(UUID bookingId, String reportUrl, UUID reportDocumentId) {
        log.info("Uploading report for booking: {}", bookingId);

        return bookingRepository.findById(bookingId)
                .flatMap(booking -> {
                    booking.setReportUrl(reportUrl);
                    booking.setReportDocumentId(reportDocumentId);
                    booking.setReportReadyAt(Instant.now());
                    booking.setStatus(LabBookingStatus.REPORT_READY);
                    booking.setUpdatedAt(Instant.now());
                    return bookingRepository.save(booking);
                })
                .flatMap(booking -> getBookingResponse(booking.getId()))
                .doOnSuccess(response -> eventPublisher.publishReportUploaded(response));
    }

    private Mono<LabBookingResponse> getBookingResponse(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .map(this::toLabBookingResponse);
    }

    private Mono<List<LabTest>> getTestsForBooking(List<UUID> testIds) {
        return Flux.fromIterable(testIds)
                .flatMap(testRepository::findById)
                .collectList();
    }

    private String generateBookingNumber() {
        String timestamp = LocalDateTime.now().format(BOOKING_NUMBER_FORMATTER);
        String random = String.valueOf((int) (Math.random() * 1000));
        return "LAB-" + timestamp + "-" + random;
    }

    private boolean canCancel(LabBookingStatus status) {
        return status == LabBookingStatus.PENDING 
            || status == LabBookingStatus.CONFIRMED 
            || status == LabBookingStatus.PHLEBOTOMIST_ASSIGNED;
    }

    private String serializeAddress(DeliveryAddress address) {
        try {
            return objectMapper.writeValueAsString(address);
        } catch (JsonProcessingException e) {
            log.error("Error serializing address", e);
            return "{}";
        }
    }

    private String serializeTests(List<LabTest> tests) {
        try {
            return objectMapper.writeValueAsString(tests.stream()
                    .map(test -> new TestReference(test.getId(), test.getName(), test.getBasePrice()))
                    .collect(Collectors.toList()));
        } catch (JsonProcessingException e) {
            log.error("Error serializing tests", e);
            return "[]";
        }
    }

    private LabTestResponse toLabTestResponse(LabTest test) {
        return LabTestResponse.builder()
                .id(test.getId())
                .testCode(test.getTestCode())
                .name(test.getName())
                .shortName(test.getShortName())
                .description(test.getDescription())
                .categoryId(test.getCategoryId())
                .sampleType(test.getSampleType())
                .sampleVolume(test.getSampleVolume())
                .fastingRequired(test.getFastingRequired())
                .fastingHours(test.getFastingHours())
                .preparationInstructions(test.getPreparationInstructions())
                .reportAvailableIn(test.getReportAvailableIn())
                .basePrice(test.getBasePrice())
                .mrp(test.getMrp())
                .isPopular(test.getIsPopular())
                .requiresDoctorReferral(test.getRequiresDoctorReferral())
                .homeCollectionAvailable(test.getHomeCollectionAvailable())
                .build();
    }

    private Mono<TestPackageResponse> toTestPackageResponse(TestPackage pkg) {
        return Flux.fromArray(pkg.getIncludedTests())
                .flatMap(testRepository::findById)
                .map(this::toLabTestResponse)
                .collectList()
                .map(tests -> TestPackageResponse.builder()
                        .id(pkg.getId())
                        .packageCode(pkg.getPackageCode())
                        .name(pkg.getName())
                        .description(pkg.getDescription())
                        .includedTests(tests)
                        .packagePrice(pkg.getPackagePrice())
                        .individualPrice(pkg.getIndividualPrice())
                        .discountPercent(pkg.getDiscountPercent())
                        .savings(pkg.getIndividualPrice().subtract(pkg.getPackagePrice()))
                        .totalParameters(pkg.getTotalParameters())
                        .sampleTypes(List.of(pkg.getSampleTypes()))
                        .fastingRequired(pkg.getFastingRequired())
                        .targetGender(pkg.getTargetGender())
                        .targetAgeGroup(pkg.getTargetAgeGroup())
                        .isPopular(pkg.getIsPopular())
                        .build());
    }

    private AvailableSlotResponse toAvailableSlotResponse(CollectionSlot slot, Partner partner) {
        return AvailableSlotResponse.builder()
                .slotId(slot.getId())
                .labPartnerId(partner.getId())
                .labPartnerName(partner.getBusinessName())
                .date(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .slotLabel(slot.getSlotLabel())
                .availableCapacity(slot.getMaxBookings() - slot.getCurrentBookings())
                .homeCollectionFee(homeCollectionFee)
                .build();
    }

    private LabBookingResponse toLabBookingResponse(LabBooking booking) {
        return LabBookingResponse.builder()
                .id(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .userId(booking.getUserId())
                .patientName(booking.getPatientName())
                .patientAge(booking.getPatientAge())
                .patientGender(booking.getPatientGender())
                .patientPhone(booking.getPatientPhone())
                .bookingType(booking.getBookingType())
                .labPartnerId(booking.getLabPartnerId())
                .labPartnerName(booking.getLabPartnerName())
                .scheduledDate(booking.getScheduledDate())
                .scheduledSlot(booking.getScheduledSlot())
                .subtotal(booking.getSubtotal())
                .discountAmount(booking.getDiscountAmount())
                .homeCollectionFee(booking.getHomeCollectionFee())
                .totalAmount(booking.getTotalAmount())
                .paymentStatus(booking.getPaymentStatus())
                .paymentId(booking.getPaymentId())
                .paidAt(booking.getPaidAt())
                .status(booking.getStatus())
                .phlebotomistName(booking.getPhlebotomistName())
                .phlebotomistPhone(booking.getPhlebotomistPhone())
                .sampleCollectedAt(booking.getSampleCollectedAt())
                .reportReadyAt(booking.getReportReadyAt())
                .reportUrl(booking.getReportUrl())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .canCancel(canCancel(booking.getStatus()))
                .canDownloadReport(booking.getReportUrl() != null)
                .build();
    }

    // Inner class for serializing test references
    private record TestReference(UUID id, String name, BigDecimal price) {}
}
