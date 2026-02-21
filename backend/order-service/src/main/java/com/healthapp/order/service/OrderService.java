package com.healthapp.order.service;

import org.springframework.context.annotation.Profile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthapp.order.domain.*;
import com.healthapp.order.domain.enums.*;
import com.healthapp.order.dto.*;
import com.healthapp.order.event.OrderEventPublisher;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for order management.
 */
@Slf4j
@Profile("!test")
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final DeliveryAddressRepository addressRepository;
    private final CartService cartService;
    private final PharmacyAssignmentService pharmacyAssignmentService;
    private final OrderEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${delivery.default-fee:30.00}")
    private BigDecimal defaultDeliveryFee;

    @Value("${delivery.free-delivery-threshold:500.00}")
    private BigDecimal freeDeliveryThreshold;

    private static final DateTimeFormatter ORDER_NUMBER_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Place a new order from cart.
     */
    @Transactional
    public Mono<OrderResponse> placeOrder(UUID userId, PlaceOrderRequest request) {
        log.info("Placing order for user: {}", userId);

        return Mono.zip(
                cartService.getCart(userId.toString()),
                addressRepository.findById(request.getDeliveryAddressId())
        ).flatMap(tuple -> {
            Cart cart = tuple.getT1();
            DeliveryAddress address = tuple.getT2();

            if (cart.isEmpty()) {
                return Mono.error(new IllegalStateException("Cart is empty"));
            }

            // Calculate totals
            BigDecimal subtotal = cart.getSubtotal();
            BigDecimal discount = cart.getDiscountAmount();
            BigDecimal deliveryFee = calculateDeliveryFee(subtotal, request.getDeliveryType());
            BigDecimal tax = calculateTax(subtotal.subtract(discount));
            BigDecimal total = subtotal.subtract(discount).add(deliveryFee).add(tax);

            // Create order
            Order order = Order.builder()
                    .orderNumber(generateOrderNumber())
                    .userId(userId)
                    .orderType(OrderType.MEDICINE)
                    .prescriptionId(request.getPrescriptionId())
                    .deliveryAddressId(request.getDeliveryAddressId())
                    .deliveryAddressSnapshot(serializeAddress(address))
                    .deliveryType(request.getDeliveryType())
                    .subtotal(subtotal)
                    .discountAmount(discount)
                    .couponCode(cart.getCouponCode())
                    .deliveryFee(deliveryFee)
                    .taxAmount(tax)
                    .totalAmount(total)
                    .currency("INR")
                    .paymentStatus(PaymentStatus.PENDING)
                    .paymentMethod(request.getPaymentMethod())
                    .status(OrderStatus.PENDING_PAYMENT)
                    .statusUpdatedAt(Instant.now())
                    .notes(request.getNotes())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            return orderRepository.save(order)
                    .flatMap(savedOrder -> saveOrderItems(savedOrder, cart.getItems())
                            .then(createStatusHistory(savedOrder, null, OrderStatus.PENDING_PAYMENT, "SYSTEM", "Order created"))
                            .then(cartService.clearCart(userId.toString()))
                            .then(Mono.just(savedOrder)));
        })
        .flatMap(order -> getOrderResponse(order.getId()))
        .doOnSuccess(response -> eventPublisher.publishOrderCreated(response));
    }

    /**
     * Get order by ID.
     */
    public Mono<OrderResponse> getOrder(UUID orderId) {
        return getOrderResponse(orderId);
    }

    /**
     * Get order by order number.
     */
    public Mono<OrderResponse> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .flatMap(order -> getOrderResponse(order.getId()));
    }

    /**
     * Get user's orders.
     */
    public Flux<OrderResponse> getUserOrders(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .flatMap(order -> getOrderResponse(order.getId()));
    }

    /**
     * Get user's orders with pagination.
     */
    public Flux<OrderResponse> getUserOrders(UUID userId, int page, int size) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .skip((long) page * size)
                .take(size)
                .flatMap(order -> getOrderResponse(order.getId()));
    }

    /**
     * Get user's orders by status with pagination.
     */
    public Flux<OrderResponse> getUserOrdersByStatus(UUID userId, OrderStatus status, int page, int size) {
        return orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status)
                .skip((long) page * size)
                .take(size)
                .flatMap(order -> getOrderResponse(order.getId()));
    }

    /**
     * Get partner's orders with pagination.
     */
    public Flux<OrderResponse> getPartnerOrders(UUID partnerId, OrderStatus status, int page, int size) {
        if (status != null) {
            return orderRepository.findByPartnerIdAndStatusOrderByCreatedAtDesc(partnerId, status)
                    .skip((long) page * size)
                    .take(size)
                    .flatMap(order -> getOrderResponse(order.getId()));
        }
        return orderRepository.findByPartnerIdOrderByCreatedAtDesc(partnerId)
                .skip((long) page * size)
                .take(size)
                .flatMap(order -> getOrderResponse(order.getId()));
    }

    /**
     * Update order status.
     */
    @Transactional
    public Mono<OrderResponse> updateOrderStatus(UUID orderId, OrderStatus newStatus, 
                                                  String changedByType, String notes) {
        log.info("Updating order status: {} to {}", orderId, newStatus);

        return orderRepository.findById(orderId)
                .flatMap(order -> {
                    OrderStatus oldStatus = order.getStatus();
                    
                    if (!isValidStatusTransition(oldStatus, newStatus)) {
                        return Mono.error(new IllegalStateException(
                            "Invalid status transition from " + oldStatus + " to " + newStatus));
                    }

                    order.setStatus(newStatus);
                    order.setStatusUpdatedAt(Instant.now());
                    order.setUpdatedAt(Instant.now());

                    if (newStatus == OrderStatus.CANCELLED) {
                        order.setCancelledAt(Instant.now());
                        order.setCancellationReason(notes);
                    }

                    if (newStatus == OrderStatus.DELIVERED) {
                        order.setActualDelivery(Instant.now());
                    }

                    return orderRepository.save(order)
                            .flatMap(saved -> createStatusHistory(saved, oldStatus, newStatus, changedByType, notes)
                                    .thenReturn(saved));
                })
                .flatMap(order -> getOrderResponse(order.getId()))
                .doOnSuccess(response -> eventPublisher.publishOrderStatusUpdated(response));
    }

    /**
     * Update order status without changedByType parameter.
     */
    @Transactional
    public Mono<OrderResponse> updateOrderStatus(UUID orderId, OrderStatus newStatus, String notes) {
        return updateOrderStatus(orderId, newStatus, "PARTNER", notes);
    }

    /**
     * Confirm payment for order.
     */
    @Transactional
    public Mono<OrderResponse> confirmPayment(UUID orderId, UUID paymentId) {
        log.info("Confirming payment for order: {}, payment: {}", orderId, paymentId);

        return orderRepository.findById(orderId)
                .flatMap(order -> {
                    order.setPaymentId(paymentId);
                    order.setPaymentStatus(PaymentStatus.COMPLETED);
                    order.setPaidAt(Instant.now());
                    order.setStatus(OrderStatus.CONFIRMED);
                    order.setStatusUpdatedAt(Instant.now());
                    order.setUpdatedAt(Instant.now());

                    return orderRepository.save(order)
                            .flatMap(saved -> createStatusHistory(saved, OrderStatus.PENDING_PAYMENT, 
                                    OrderStatus.CONFIRMED, "SYSTEM", "Payment confirmed")
                                    .thenReturn(saved));
                })
                // Assign pharmacy after payment
                .flatMap(order -> pharmacyAssignmentService.assignPharmacy(order.getId())
                        .thenReturn(order))
                .flatMap(order -> getOrderResponse(order.getId()))
                .doOnSuccess(response -> eventPublisher.publishOrderConfirmed(response));
    }

    /**
     * Confirm payment for order with paymentId and transactionId.
     */
    @Transactional
    public Mono<OrderResponse> confirmPayment(UUID orderId, String paymentId, String transactionId) {
        log.info("Confirming payment for order: {}, payment: {}, transaction: {}", orderId, paymentId, transactionId);
        return confirmPayment(orderId, UUID.fromString(paymentId));
    }

    /**
     * Cancel order.
     */
    @Transactional
    public Mono<OrderResponse> cancelOrder(UUID orderId, UUID userId, String reason) {
        log.info("Cancelling order: {} for user: {}", orderId, userId);

        return orderRepository.findById(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .filter(order -> canCancel(order.getStatus()))
                .switchIfEmpty(Mono.error(new IllegalStateException("Order cannot be cancelled")))
                .flatMap(order -> updateOrderStatus(orderId, OrderStatus.CANCELLED, "CUSTOMER", reason))
                .doOnSuccess(response -> eventPublisher.publishOrderCancelled(response));
    }

    /**
     * Cancel order without userId check.
     */
    @Transactional
    public Mono<OrderResponse> cancelOrder(UUID orderId, String reason) {
        log.info("Cancelling order: {}", orderId);

        return orderRepository.findById(orderId)
                .filter(order -> canCancel(order.getStatus()))
                .switchIfEmpty(Mono.error(new IllegalStateException("Order cannot be cancelled")))
                .flatMap(order -> updateOrderStatus(orderId, OrderStatus.CANCELLED, "CUSTOMER", reason))
                .doOnSuccess(response -> eventPublisher.publishOrderCancelled(response));
    }

    /**
     * Get tracking info for order.
     */
    public Mono<TrackingInfo> getTrackingInfo(UUID orderId) {
        return orderRepository.findById(orderId)
                .flatMap(order -> statusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId)
                        .collectList()
                        .map(history -> buildTrackingInfo(order, history)));
    }

    private Mono<OrderResponse> getOrderResponse(UUID orderId) {
        return orderRepository.findById(orderId)
                .flatMap(order -> orderItemRepository.findByOrderId(orderId)
                        .collectList()
                        .map(items -> toOrderResponse(order, items)));
    }

    private Mono<Void> saveOrderItems(Order order, List<CartItem> cartItems) {
        List<OrderItem> items = cartItems.stream()
                .map(cartItem -> OrderItem.builder()
                        .orderId(order.getId())
                        .itemType("MEDICINE")
                        .productId(cartItem.getProductId())
                        .productName(cartItem.getProductName())
                        .manufacturer(cartItem.getManufacturer())
                        .strength(cartItem.getStrength())
                        .formulation(cartItem.getFormulation())
                        .packSize(cartItem.getPackSize())
                        .unitPrice(cartItem.getUnitPrice())
                        .quantity(cartItem.getQuantity())
                        .discountPercent(BigDecimal.ZERO)
                        .discountAmount(BigDecimal.ZERO)
                        .taxPercent(BigDecimal.valueOf(5))
                        .taxAmount(cartItem.getTotal().multiply(BigDecimal.valueOf(0.05)))
                        .totalPrice(cartItem.getTotal())
                        .prescriptionItemId(cartItem.getPrescriptionItemId() != null 
                            ? UUID.fromString(cartItem.getPrescriptionItemId()) : null)
                        .requiresPrescription(cartItem.getRequiresPrescription())
                        .isAvailable(true)
                        .createdAt(Instant.now())
                        .build())
                .collect(Collectors.toList());

        return orderItemRepository.saveAll(items).then();
    }

    private Mono<Void> createStatusHistory(Order order, OrderStatus fromStatus, 
                                            OrderStatus toStatus, String changedByType, String notes) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .orderId(order.getId())
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedByType(changedByType)
                .notes(notes)
                .createdAt(Instant.now())
                .build();

        return statusHistoryRepository.save(history).then();
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(ORDER_NUMBER_FORMATTER);
        String random = String.valueOf((int) (Math.random() * 1000));
        return "ORD-" + timestamp + "-" + random;
    }

    private BigDecimal calculateDeliveryFee(BigDecimal subtotal, DeliveryType deliveryType) {
        if (subtotal.compareTo(freeDeliveryThreshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return deliveryType == DeliveryType.EXPRESS 
            ? defaultDeliveryFee.multiply(BigDecimal.valueOf(1.5))
            : defaultDeliveryFee;
    }

    private BigDecimal calculateTax(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(0.05)).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private boolean isValidStatusTransition(OrderStatus from, OrderStatus to) {
        // Define valid transitions
        return switch (from) {
            case PENDING_PAYMENT -> to == OrderStatus.CONFIRMED || to == OrderStatus.PAYMENT_FAILED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING -> to == OrderStatus.PACKED || to == OrderStatus.CANCELLED;
            case PACKED -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED -> to == OrderStatus.OUT_FOR_DELIVERY || to == OrderStatus.CANCELLED;
            case OUT_FOR_DELIVERY -> to == OrderStatus.DELIVERED || to == OrderStatus.CANCELLED;
            case DELIVERED -> to == OrderStatus.RETURN_REQUESTED;
            case RETURN_REQUESTED -> to == OrderStatus.RETURNED;
            case RETURNED -> to == OrderStatus.REFUNDED;
            default -> false;
        };
    }

    private boolean canCancel(OrderStatus status) {
        return status == OrderStatus.PENDING_PAYMENT 
            || status == OrderStatus.CONFIRMED 
            || status == OrderStatus.PROCESSING;
    }

    private String serializeAddress(DeliveryAddress address) {
        try {
            return objectMapper.writeValueAsString(address);
        } catch (JsonProcessingException e) {
            log.error("Error serializing address", e);
            return "{}";
        }
    }

    private TrackingInfo buildTrackingInfo(Order order, List<OrderStatusHistory> history) {
        List<TrackingStep> steps = List.of(
            buildStep(OrderStatus.CONFIRMED, "Order Confirmed", "Your order has been placed", order, history),
            buildStep(OrderStatus.PROCESSING, "Processing", "Partner is preparing your order", order, history),
            buildStep(OrderStatus.PACKED, "Packed", "Your order has been packed", order, history),
            buildStep(OrderStatus.SHIPPED, "Shipped", "Order handed to delivery partner", order, history),
            buildStep(OrderStatus.OUT_FOR_DELIVERY, "Out for Delivery", "Delivery partner is on the way", order, history),
            buildStep(OrderStatus.DELIVERED, "Delivered", "Order delivered successfully", order, history)
        );

        return TrackingInfo.builder()
                .orderId(order.getId().toString())
                .orderNumber(order.getOrderNumber())
                .currentStatus(order.getStatus())
                .statusText(getStatusText(order.getStatus()))
                .trackingNumber(order.getTrackingNumber())
                .deliveryPartner(order.getDeliveryPartner())
                .estimatedDelivery(order.getEstimatedDelivery())
                .partnerName(order.getPartnerName())
                .steps(steps)
                .canCancel(canCancel(order.getStatus()))
                .canReturn(order.getStatus() == OrderStatus.DELIVERED)
                .build();
    }

    private TrackingStep buildStep(OrderStatus status, String title, String description, 
                                    Order order, List<OrderStatusHistory> history) {
        Instant timestamp = history.stream()
                .filter(h -> h.getToStatus() == status)
                .map(OrderStatusHistory::getCreatedAt)
                .findFirst()
                .orElse(null);

        return TrackingStep.builder()
                .status(status)
                .title(title)
                .description(description)
                .timestamp(timestamp)
                .isComplete(order.getStatus().ordinal() >= status.ordinal())
                .isCurrent(order.getStatus() == status)
                .build();
    }

    private String getStatusText(OrderStatus status) {
        return switch (status) {
            case CART -> "In Cart";
            case PENDING_PAYMENT -> "Awaiting Payment";
            case PAYMENT_FAILED -> "Payment Failed";
            case CONFIRMED -> "Order Confirmed";
            case PROCESSING -> "Processing";
            case PACKED -> "Packed";
            case SHIPPED -> "Shipped";
            case OUT_FOR_DELIVERY -> "Out for Delivery";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
            case RETURN_REQUESTED -> "Return Requested";
            case RETURNED -> "Returned";
            case REFUNDED -> "Refunded";
        };
    }

    private OrderResponse toOrderResponse(Order order, List<OrderItem> items) {
        List<OrderItemResponse> itemResponses = items.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .orderType(order.getOrderType())
                .partnerId(order.getPartnerId())
                .partnerName(order.getPartnerName())
                .partnerType(order.getPartnerType())
                .items(itemResponses)
                .deliveryType(order.getDeliveryType())
                .scheduledDeliveryDate(order.getScheduledDeliveryDate())
                .scheduledDeliverySlot(order.getScheduledDeliverySlot())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .couponCode(order.getCouponCode())
                .deliveryFee(order.getDeliveryFee())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .paymentStatus(order.getPaymentStatus())
                .paymentId(order.getPaymentId())
                .paymentMethod(order.getPaymentMethod())
                .paidAt(order.getPaidAt())
                .status(order.getStatus())
                .statusUpdatedAt(order.getStatusUpdatedAt())
                .trackingNumber(order.getTrackingNumber())
                .deliveryPartner(order.getDeliveryPartner())
                .estimatedDelivery(order.getEstimatedDelivery())
                .actualDelivery(order.getActualDelivery())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .canCancel(canCancel(order.getStatus()))
                .canReturn(order.getStatus() == OrderStatus.DELIVERED)
                .canReorder(order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED)
                .build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .itemType(item.getItemType())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productDescription(item.getProductDescription())
                .manufacturer(item.getManufacturer())
                .strength(item.getStrength())
                .formulation(item.getFormulation())
                .packSize(item.getPackSize())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .discountPercent(item.getDiscountPercent())
                .discountAmount(item.getDiscountAmount())
                .taxAmount(item.getTaxAmount())
                .totalPrice(item.getTotalPrice())
                .requiresPrescription(item.getRequiresPrescription())
                .prescriptionItemId(item.getPrescriptionItemId())
                .isAvailable(item.getIsAvailable())
                .substitutedWith(item.getSubstitutedWith())
                .fulfilledQuantity(item.getFulfilledQuantity())
                .build();
    }
}
