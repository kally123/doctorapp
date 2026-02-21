package com.healthapp.order.controller;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.dto.*;
import com.healthapp.order.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for cart management operations.
 */
@Slf4j
@Profile("!test")
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management APIs")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get cart", description = "Get the current user's shopping cart")
    public Mono<ResponseEntity<CartResponse>> getCart(
            @RequestHeader("X-User-Id") UUID userId) {
        log.info("Getting cart for user: {}", userId);
        return cartService.getCart(userId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Add a medicine item to the shopping cart")
    public Mono<ResponseEntity<CartResponse>> addToCart(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("Adding item to cart for user: {}, product: {}", userId, request.getProductId());
        return cartService.addItem(userId, request)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Update cart item", description = "Update quantity of an item in the cart")
    public Mono<ResponseEntity<CartResponse>> updateCartItem(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("Updating cart item for user: {}, product: {}", userId, productId);
        return cartService.updateItem(userId, productId, request)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart", description = "Remove an item from the shopping cart")
    public Mono<ResponseEntity<CartResponse>> removeFromCart(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable String productId) {
        log.info("Removing item from cart for user: {}, product: {}", userId, productId);
        return cartService.removeItem(userId, productId)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Clear all items from the shopping cart")
    public Mono<ResponseEntity<Void>> clearCart(
            @RequestHeader("X-User-Id") UUID userId) {
        log.info("Clearing cart for user: {}", userId);
        return cartService.clearCart(userId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @PostMapping("/coupon")
    @Operation(summary = "Apply coupon", description = "Apply a discount coupon to the cart")
    public Mono<ResponseEntity<CartResponse>> applyCoupon(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam String couponCode) {
        log.info("Applying coupon {} for user: {}", couponCode, userId);
        return cartService.applyCoupon(userId, couponCode)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/coupon")
    @Operation(summary = "Remove coupon", description = "Remove applied coupon from the cart")
    public Mono<ResponseEntity<CartResponse>> removeCoupon(
            @RequestHeader("X-User-Id") UUID userId) {
        log.info("Removing coupon for user: {}", userId);
        return cartService.removeCoupon(userId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get cart summary", description = "Get cart summary with pricing details")
    public Mono<ResponseEntity<CartSummary>> getCartSummary(
            @RequestHeader("X-User-Id") UUID userId) {
        log.info("Getting cart summary for user: {}", userId);
        return cartService.getCartSummary(userId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/prescription/{prescriptionId}")
    @Operation(summary = "Add prescription items", description = "Add all items from a prescription to the cart")
    public Mono<ResponseEntity<CartResponse>> addFromPrescription(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID prescriptionId) {
        log.info("Adding prescription items to cart for user: {}, prescription: {}", userId, prescriptionId);
        return cartService.addFromPrescription(userId, prescriptionId)
                .map(ResponseEntity::ok);
    }
}
