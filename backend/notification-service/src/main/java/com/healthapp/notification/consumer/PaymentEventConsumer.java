package com.healthapp.notification.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthapp.notification.domain.NotificationType;
import com.healthapp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "payment-events", groupId = "notification-service")
    public void handlePaymentEvent(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();
            
            log.info("Received payment event: {}", eventType);
            
            switch (eventType) {
                case "PAYMENT_COMPLETED":
                    handlePaymentCompleted(event);
                    break;
                case "PAYMENT_FAILED":
                    handlePaymentFailed(event);
                    break;
                case "REFUND_PROCESSED":
                    handleRefundProcessed(event);
                    break;
                default:
                    log.debug("Ignoring payment event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing payment event", e);
        }
    }
    
    private void handlePaymentCompleted(JsonNode event) {
        try {
            String transactionId = event.get("transactionId").asText();
            UUID patientId = UUID.fromString(event.get("patientId").asText());
            String amount = event.has("amount") ? event.get("amount").asText() : "0";
            String currency = event.has("currency") ? event.get("currency").asText() : "INR";
            String appointmentId = event.has("appointmentId") ? event.get("appointmentId").asText() : null;
            String doctorName = event.has("doctorName") ? event.get("doctorName").asText() : "Doctor";
            
            Map<String, Object> context = new HashMap<>();
            context.put("transactionId", transactionId);
            context.put("amount", amount);
            context.put("currency", currency);
            context.put("doctorName", doctorName);
            if (appointmentId != null) {
                context.put("appointmentId", appointmentId);
            }
            
            notificationService.sendMultiChannel(
                    patientId, 
                    NotificationType.PAYMENT_RECEIVED, 
                    context, 
                    "PAYMENT", 
                    transactionId)
                    .subscribe();
            
        } catch (Exception e) {
            log.error("Error handling payment completed event", e);
        }
    }
    
    private void handlePaymentFailed(JsonNode event) {
        try {
            String transactionId = event.get("transactionId").asText();
            UUID patientId = UUID.fromString(event.get("patientId").asText());
            String reason = event.has("reason") ? event.get("reason").asText() : "Payment processing failed";
            String amount = event.has("amount") ? event.get("amount").asText() : "0";
            
            Map<String, Object> context = new HashMap<>();
            context.put("transactionId", transactionId);
            context.put("reason", reason);
            context.put("amount", amount);
            
            notificationService.sendMultiChannel(
                    patientId, 
                    NotificationType.PAYMENT_FAILED, 
                    context, 
                    "PAYMENT", 
                    transactionId)
                    .subscribe();
            
        } catch (Exception e) {
            log.error("Error handling payment failed event", e);
        }
    }
    
    private void handleRefundProcessed(JsonNode event) {
        try {
            String refundId = event.get("refundId").asText();
            UUID patientId = UUID.fromString(event.get("patientId").asText());
            String amount = event.has("amount") ? event.get("amount").asText() : "0";
            String currency = event.has("currency") ? event.get("currency").asText() : "INR";
            String originalTransactionId = event.has("originalTransactionId") ? event.get("originalTransactionId").asText() : "";
            
            Map<String, Object> context = new HashMap<>();
            context.put("refundId", refundId);
            context.put("amount", amount);
            context.put("currency", currency);
            context.put("originalTransactionId", originalTransactionId);
            
            notificationService.sendMultiChannel(
                    patientId, 
                    NotificationType.REFUND_PROCESSED, 
                    context, 
                    "REFUND", 
                    refundId)
                    .subscribe();
            
        } catch (Exception e) {
            log.error("Error handling refund processed event", e);
        }
    }
}
