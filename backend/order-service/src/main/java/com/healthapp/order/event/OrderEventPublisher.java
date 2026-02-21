package com.healthapp.order.event;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisher for order-related Kafka events.
 */
@Slf4j
@Profile("!test")
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.order-events:order-events}")
    private String orderEventsTopic;

    public void publishOrderCreated(OrderResponse order) {
        OrderEvent event = OrderEvent.builder()
                .eventType("ORDER_CREATED")
                .orderId(order.getId().toString())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId().toString())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .build();

        publishEvent(event);
        log.info("Published ORDER_CREATED event for order: {}", order.getOrderNumber());
    }

    public void publishOrderConfirmed(OrderResponse order) {
        OrderEvent event = OrderEvent.builder()
                .eventType("ORDER_CONFIRMED")
                .orderId(order.getId().toString())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId().toString())
                .partnerId(order.getPartnerId() != null ? order.getPartnerId().toString() : null)
                .status(order.getStatus().name())
                .build();

        publishEvent(event);
        log.info("Published ORDER_CONFIRMED event for order: {}", order.getOrderNumber());
    }

    public void publishOrderStatusUpdated(OrderResponse order) {
        OrderEvent event = OrderEvent.builder()
                .eventType("ORDER_STATUS_UPDATED")
                .orderId(order.getId().toString())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId().toString())
                .status(order.getStatus().name())
                .build();

        publishEvent(event);
        log.info("Published ORDER_STATUS_UPDATED event for order: {}", order.getOrderNumber());
    }

    public void publishOrderCancelled(OrderResponse order) {
        OrderEvent event = OrderEvent.builder()
                .eventType("ORDER_CANCELLED")
                .orderId(order.getId().toString())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId().toString())
                .status(order.getStatus().name())
                .build();

        publishEvent(event);
        log.info("Published ORDER_CANCELLED event for order: {}", order.getOrderNumber());
    }

    private void publishEvent(OrderEvent event) {
        kafkaTemplate.send(orderEventsTopic, event.getOrderId(), event);
    }
}
