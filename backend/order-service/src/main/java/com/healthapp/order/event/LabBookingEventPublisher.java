package com.healthapp.order.event;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.dto.LabBookingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisher for lab booking-related Kafka events.
 */
@Slf4j
@Profile("!test")
@Component
@RequiredArgsConstructor
public class LabBookingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.lab-booking-events:lab-booking-events}")
    private String labBookingEventsTopic;

    public void publishBookingCreated(LabBookingResponse booking) {
        LabBookingEvent event = LabBookingEvent.builder()
                .eventType("LAB_BOOKING_CREATED")
                .bookingId(booking.getId().toString())
                .bookingNumber(booking.getBookingNumber())
                .userId(booking.getUserId().toString())
                .labPartnerId(booking.getLabPartnerId().toString())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus().name())
                .build();

        publishEvent(event);
        log.info("Published LAB_BOOKING_CREATED event for booking: {}", booking.getBookingNumber());
    }

    public void publishBookingConfirmed(LabBookingResponse booking) {
        LabBookingEvent event = LabBookingEvent.builder()
                .eventType("LAB_BOOKING_CONFIRMED")
                .bookingId(booking.getId().toString())
                .bookingNumber(booking.getBookingNumber())
                .userId(booking.getUserId().toString())
                .labPartnerId(booking.getLabPartnerId().toString())
                .status(booking.getStatus().name())
                .scheduledDate(booking.getScheduledDate().toString())
                .scheduledSlot(booking.getScheduledSlot())
                .build();

        publishEvent(event);
        log.info("Published LAB_BOOKING_CONFIRMED event for booking: {}", booking.getBookingNumber());
    }

    public void publishBookingStatusUpdated(LabBookingResponse booking) {
        LabBookingEvent event = LabBookingEvent.builder()
                .eventType("LAB_BOOKING_STATUS_UPDATED")
                .bookingId(booking.getId().toString())
                .bookingNumber(booking.getBookingNumber())
                .userId(booking.getUserId().toString())
                .status(booking.getStatus().name())
                .build();

        publishEvent(event);
        log.info("Published LAB_BOOKING_STATUS_UPDATED event for booking: {}", booking.getBookingNumber());
    }

    public void publishBookingCancelled(LabBookingResponse booking) {
        LabBookingEvent event = LabBookingEvent.builder()
                .eventType("LAB_BOOKING_CANCELLED")
                .bookingId(booking.getId().toString())
                .bookingNumber(booking.getBookingNumber())
                .userId(booking.getUserId().toString())
                .status(booking.getStatus().name())
                .build();

        publishEvent(event);
        log.info("Published LAB_BOOKING_CANCELLED event for booking: {}", booking.getBookingNumber());
    }

    public void publishReportUploaded(LabBookingResponse booking) {
        LabBookingEvent event = LabBookingEvent.builder()
                .eventType("LAB_REPORT_UPLOADED")
                .bookingId(booking.getId().toString())
                .bookingNumber(booking.getBookingNumber())
                .userId(booking.getUserId().toString())
                .reportUrl(booking.getReportUrl())
                .status(booking.getStatus().name())
                .build();

        publishEvent(event);
        log.info("Published LAB_REPORT_UPLOADED event for booking: {}", booking.getBookingNumber());
    }

    private void publishEvent(LabBookingEvent event) {
        kafkaTemplate.send(labBookingEventsTopic, event.getBookingId(), event);
    }
}
