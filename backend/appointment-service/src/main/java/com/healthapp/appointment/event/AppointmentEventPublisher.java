package com.healthapp.appointment.event;

import com.healthapp.appointment.domain.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String TOPIC_APPOINTMENTS = "appointments";
    
    public void publishReserved(Appointment appointment) {
        AppointmentEvent event = createEvent(appointment, "RESERVED");
        publish(event);
    }
    
    public void publishConfirmed(Appointment appointment) {
        AppointmentEvent event = createEvent(appointment, "CONFIRMED");
        publish(event);
    }
    
    public void publishCancelled(Appointment appointment) {
        AppointmentEvent event = createEvent(appointment, "CANCELLED");
        publish(event);
    }
    
    public void publishExpired(Appointment appointment) {
        AppointmentEvent event = createEvent(appointment, "EXPIRED");
        publish(event);
    }
    
    public void publishRescheduled(Appointment appointment) {
        AppointmentEvent event = createEvent(appointment, "RESCHEDULED");
        publish(event);
    }
    
    public void publishCompleted(Appointment appointment) {
        AppointmentEvent event = createEvent(appointment, "COMPLETED");
        publish(event);
    }
    
    private AppointmentEvent createEvent(Appointment appointment, String eventType) {
        return AppointmentEvent.builder()
                .eventType(eventType)
                .appointmentId(appointment.getId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .scheduledAt(appointment.getScheduledAt())
                .consultationType(appointment.getConsultationType().name())
                .status(appointment.getStatus().name())
                .totalAmount(appointment.getTotalAmount())
                .currency(appointment.getCurrency())
                .build();
    }
    
    private void publish(AppointmentEvent event) {
        try {
            kafkaTemplate.send(TOPIC_APPOINTMENTS, event.getAppointmentId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish appointment event: {}", event.getEventType(), ex);
                        } else {
                            log.info("Published appointment event: {} for appointment {}", 
                                    event.getEventType(), event.getAppointmentId());
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing appointment event", e);
        }
    }
}
