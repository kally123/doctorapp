package com.healthapp.payment.event;

import com.healthapp.payment.domain.PaymentTransaction;
import com.healthapp.payment.domain.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String TOPIC_PAYMENTS = "payments";
    
    public void publishPaymentCompleted(PaymentTransaction payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventType("COMPLETED")
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .orderType(payment.getOrderType())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .build();
        
        publish(event);
    }
    
    public void publishPaymentFailed(PaymentTransaction payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventType("FAILED")
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .orderType(payment.getOrderType())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .failureReason(payment.getFailureReason())
                .build();
        
        publish(event);
    }
    
    public void publishRefundProcessed(Refund refund) {
        RefundEvent event = RefundEvent.builder()
                .eventType("REFUND_PROCESSED")
                .refundId(refund.getId())
                .paymentId(refund.getPaymentId())
                .amount(refund.getAmount())
                .currency(refund.getCurrency())
                .status(refund.getStatus())
                .reason(refund.getReason())
                .build();
        
        publishRefund(event);
    }
    
    private void publish(PaymentEvent event) {
        try {
            kafkaTemplate.send(TOPIC_PAYMENTS, event.getPaymentId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish payment event: {}", event.getEventType(), ex);
                        } else {
                            log.info("Published payment event: {} for payment {}", 
                                    event.getEventType(), event.getPaymentId());
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing payment event", e);
        }
    }
    
    private void publishRefund(RefundEvent event) {
        try {
            kafkaTemplate.send(TOPIC_PAYMENTS, event.getRefundId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish refund event: {}", event.getEventType(), ex);
                        } else {
                            log.info("Published refund event: {} for refund {}", 
                                    event.getEventType(), event.getRefundId());
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing refund event", e);
        }
    }
}
