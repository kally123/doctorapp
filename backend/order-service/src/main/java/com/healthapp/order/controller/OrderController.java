package com.healthapp.order.controller;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.dto.OrderResponse;
import com.healthapp.order.dto.PlaceOrderRequest;
import com.healthapp.order.dto.TrackingInfo;
import com.healthapp.order.domain.enums.OrderStatus;
import com.healthapp.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for order management operations.
 */
@Slf4j
@Profile("!test")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place order", description = "Place a new order from the shopping cart")
    public Mono<ResponseEntity<OrderResponse>> placeOrder(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody PlaceOrderRequest request) {
        log.info("Placing order for user: {}", userId);
        return orderService.placeOrder(userId, request)
                .map(order -> ResponseEntity.status(HttpStatus.CREATED).body(order));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order", description = "Get order details by ID")
    public Mono<ResponseEntity<OrderResponse>> getOrder(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID orderId) {
        log.info("Getting order: {} for user: {}", orderId, userId);
        return orderService.getOrder(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by number", description = "Get order details by order number")
    public Mono<ResponseEntity<OrderResponse>> getOrderByNumber(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable String orderNumber) {
        log.info("Getting order by number: {} for user: {}", orderNumber, userId);
        return orderService.getOrderByNumber(orderNumber)
                .filter(order -> order.getUserId().equals(userId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get user orders", description = "Get all orders for the current user")
    public Flux<OrderResponse> getUserOrders(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting orders for user: {}, status: {}", userId, status);
        if (status != null) {
            return orderService.getUserOrdersByStatus(userId, status, page, size);
        }
        return orderService.getUserOrders(userId, page, size);
    }

    @GetMapping("/{orderId}/tracking")
    @Operation(summary = "Get order tracking", description = "Get real-time tracking information for an order")
    public Mono<ResponseEntity<TrackingInfo>> getOrderTracking(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID orderId) {
        log.info("Getting tracking for order: {}", orderId);
        return orderService.getOrder(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .flatMap(order -> orderService.getTrackingInfo(orderId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{orderId}/confirm-payment")
    @Operation(summary = "Confirm payment", description = "Confirm payment for an order")
    public Mono<ResponseEntity<OrderResponse>> confirmPayment(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID orderId,
            @RequestParam String paymentId,
            @RequestParam String transactionId) {
        log.info("Confirming payment for order: {}, payment: {}", orderId, paymentId);
        return orderService.getOrder(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .flatMap(order -> orderService.confirmPayment(orderId, paymentId, transactionId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order")
    public Mono<ResponseEntity<OrderResponse>> cancelOrder(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID orderId,
            @RequestParam String reason) {
        log.info("Cancelling order: {} for user: {}, reason: {}", orderId, userId, reason);
        return orderService.getOrder(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .flatMap(order -> orderService.cancelOrder(orderId, reason))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Partner endpoints
    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update order status (Partner only)")
    public Mono<ResponseEntity<OrderResponse>> updateOrderStatus(
            @RequestHeader("X-Partner-Id") UUID partnerId,
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status,
            @RequestParam(required = false) String notes) {
        log.info("Updating order: {} status to: {} by partner: {}", orderId, status, partnerId);
        return orderService.updateOrderStatus(orderId, status, notes)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/partner")
    @Operation(summary = "Get partner orders", description = "Get all orders assigned to a partner")
    public Flux<OrderResponse> getPartnerOrders(
            @RequestHeader("X-Partner-Id") UUID partnerId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting orders for partner: {}, status: {}", partnerId, status);
        return orderService.getPartnerOrders(partnerId, status, page, size);
    }
}
