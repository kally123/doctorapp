package com.healthapp.order.service;

import com.healthapp.order.domain.Cart;
import com.healthapp.order.domain.CartItem;
import com.healthapp.order.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for cart management with Redis storage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final ReactiveRedisTemplate<String, Cart> cartRedisTemplate;
    private final AddressService addressService;

    @Value("${cart.expiry-days:7}")
    private int cartExpiryDays;

    @Value("${delivery.default-fee:30.00}")
    private BigDecimal defaultDeliveryFee;

    @Value("${delivery.free-delivery-threshold:500.00}")
    private BigDecimal freeDeliveryThreshold;

    private static final String CART_KEY_PREFIX = "cart:";

    /**
     * Get cart for user, create empty if not exists.
     */
    public Mono<Cart> getCart(String userId) {
        String key = buildCartKey(userId);
        return cartRedisTemplate.opsForValue().get(key)
                .defaultIfEmpty(Cart.empty(userId));
    }

    /**
     * Get cart for user by UUID.
     */
    public Mono<CartResponse> getCart(UUID userId) {
        return getCartResponse(userId.toString());
    }

    /**
     * Get cart as response DTO.
     */
    public Mono<CartResponse> getCartResponse(String userId) {
        return getCart(userId)
                .map(this::toCartResponse);
    }

    /**
     * Add item to cart.
     */
    public Mono<CartResponse> addItem(String userId, AddToCartRequest request) {
        log.info("Adding item to cart for user: {}, product: {}", userId, request.getProductId());
        
        CartItem item = CartItem.builder()
                .itemId(UUID.randomUUID().toString())
                .productId(request.getProductId())
                .productName(request.getProductName())
                .manufacturer(request.getManufacturer())
                .strength(request.getStrength())
                .formulation(request.getFormulation())
                .packSize(request.getPackSize())
                .unitPrice(request.getUnitPrice())
                .mrp(request.getMrp())
                .quantity(request.getQuantity())
                .requiresPrescription(request.getRequiresPrescription())
                .prescriptionId(request.getPrescriptionId())
                .prescriptionItemId(request.getPrescriptionItemId())
                .imageUrl(request.getImageUrl())
                .build();

        return getCart(userId)
                .map(cart -> cart.addItem(item))
                .flatMap(cart -> saveCart(userId, cart))
                .map(this::toCartResponse);
    }

    /**
     * Add item to cart by UUID.
     */
    public Mono<CartResponse> addItem(UUID userId, AddToCartRequest request) {
        return addItem(userId.toString(), request);
    }

    /**
     * Update item quantity in cart.
     */
    public Mono<CartResponse> updateQuantity(String userId, String itemId, int quantity) {
        log.info("Updating cart item quantity for user: {}, item: {}, quantity: {}", 
                userId, itemId, quantity);
        
        if (quantity <= 0) {
            return removeItem(userId, itemId);
        }

        return getCart(userId)
                .map(cart -> cart.updateQuantity(itemId, quantity))
                .flatMap(cart -> saveCart(userId, cart))
                .map(this::toCartResponse);
    }

    /**
     * Remove item from cart.
     */
    public Mono<CartResponse> removeItem(String userId, String itemId) {
        log.info("Removing item from cart for user: {}, item: {}", userId, itemId);
        
        return getCart(userId)
                .map(cart -> cart.removeItem(itemId))
                .flatMap(cart -> saveCart(userId, cart))
                .map(this::toCartResponse);
    }

    /**
     * Remove item from cart by UUID.
     */
    public Mono<CartResponse> removeItem(UUID userId, String productId) {
        return removeItem(userId.toString(), productId);
    }

    /**
     * Update item in cart.
     */
    public Mono<CartResponse> updateItem(UUID userId, String productId, UpdateCartItemRequest request) {
        return updateQuantity(userId.toString(), productId, request.getQuantity());
    }

    /**
     * Apply coupon to cart.
     */
    public Mono<CartResponse> applyCoupon(String userId, String couponCode) {
        log.info("Applying coupon to cart for user: {}, code: {}", userId, couponCode);
        
        // TODO: Validate coupon with coupon service
        // For now, apply a 10% discount for demo
        return getCart(userId)
                .map(cart -> {
                    BigDecimal discount = cart.getSubtotal().multiply(BigDecimal.valueOf(0.10));
                    return cart.applyCoupon(couponCode, discount);
                })
                .flatMap(cart -> saveCart(userId, cart))
                .map(this::toCartResponse);
    }

    /**
     * Apply coupon to cart by UUID.
     */
    public Mono<CartResponse> applyCoupon(UUID userId, String couponCode) {
        return applyCoupon(userId.toString(), couponCode);
    }

    /**
     * Remove coupon from cart.
     */
    public Mono<CartResponse> removeCoupon(String userId) {
        log.info("Removing coupon from cart for user: {}", userId);
        
        return getCart(userId)
                .map(Cart::removeCoupon)
                .flatMap(cart -> saveCart(userId, cart))
                .map(this::toCartResponse);
    }

    /**
     * Remove coupon from cart by UUID.
     */
    public Mono<CartResponse> removeCoupon(UUID userId) {
        return removeCoupon(userId.toString());
    }

    /**
     * Get cart summary with delivery details.
     */
    public Mono<CartSummary> getCartSummary(String userId, UUID addressId) {
        return Mono.zip(
                getCart(userId),
                addressService.getAddress(addressId)
        ).map(tuple -> {
            Cart cart = tuple.getT1();
            AddressResponse address = tuple.getT2();

            BigDecimal subtotal = cart.getSubtotal();
            BigDecimal discount = cart.getDiscountAmount();
            BigDecimal deliveryFee = calculateDeliveryFee(subtotal);
            BigDecimal tax = calculateTax(subtotal.subtract(discount));
            BigDecimal total = subtotal.subtract(discount).add(deliveryFee).add(tax);

            return CartSummary.builder()
                    .items(toCartItemResponses(cart.getItems()))
                    .itemCount(cart.getItemCount())
                    .totalItems(cart.getTotalItems())
                    .subtotal(subtotal)
                    .couponCode(cart.getCouponCode())
                    .discount(discount)
                    .deliveryFee(deliveryFee)
                    .tax(tax)
                    .total(total)
                    .deliveryAddress(address)
                    .estimatedDelivery(Instant.now().plus(Duration.ofDays(2)))
                    .freeDeliveryEligible(subtotal.compareTo(freeDeliveryThreshold) >= 0)
                    .amountForFreeDelivery(
                        subtotal.compareTo(freeDeliveryThreshold) < 0 
                            ? freeDeliveryThreshold.subtract(subtotal) 
                            : BigDecimal.ZERO)
                    .build();
        });
    }

    /**
     * Get cart summary without address.
     */
    public Mono<CartSummary> getCartSummary(UUID userId) {
        return getCart(userId.toString())
                .map(cart -> {
                    BigDecimal subtotal = cart.getSubtotal();
                    BigDecimal discount = cart.getDiscountAmount();
                    BigDecimal deliveryFee = calculateDeliveryFee(subtotal);
                    BigDecimal tax = calculateTax(subtotal.subtract(discount));
                    BigDecimal total = subtotal.subtract(discount).add(deliveryFee).add(tax);

                    return CartSummary.builder()
                            .items(toCartItemResponses(cart.getItems()))
                            .itemCount(cart.getItemCount())
                            .totalItems(cart.getTotalItems())
                            .subtotal(subtotal)
                            .couponCode(cart.getCouponCode())
                            .discount(discount)
                            .deliveryFee(deliveryFee)
                            .tax(tax)
                            .total(total)
                            .estimatedDelivery(Instant.now().plus(Duration.ofDays(2)))
                            .freeDeliveryEligible(subtotal.compareTo(freeDeliveryThreshold) >= 0)
                            .amountForFreeDelivery(
                                subtotal.compareTo(freeDeliveryThreshold) < 0 
                                    ? freeDeliveryThreshold.subtract(subtotal) 
                                    : BigDecimal.ZERO)
                            .build();
                });
    }

    /**
     * Add items from prescription.
     */
    public Mono<CartResponse> addFromPrescription(UUID userId, UUID prescriptionId) {
        log.info("Adding prescription items to cart for user: {}, prescription: {}", userId, prescriptionId);
        // TODO: Integrate with prescription service to get items
        // For now, return current cart
        return getCart(userId);
    }

    /**
     * Clear cart after order placement.
     */
    public Mono<Void> clearCart(String userId) {
        String key = buildCartKey(userId);
        return cartRedisTemplate.delete(key).then();
    }

    /**
     * Clear cart by UUID.
     */
    public Mono<Void> clearCart(UUID userId) {
        return clearCart(userId.toString());
    }

    private Mono<Cart> saveCart(String userId, Cart cart) {
        String key = buildCartKey(userId);
        Duration expiry = Duration.ofDays(cartExpiryDays);
        return cartRedisTemplate.opsForValue()
                .set(key, cart, expiry)
                .thenReturn(cart);
    }

    private String buildCartKey(String userId) {
        return CART_KEY_PREFIX + userId;
    }

    private BigDecimal calculateDeliveryFee(BigDecimal subtotal) {
        if (subtotal.compareTo(freeDeliveryThreshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return defaultDeliveryFee;
    }

    private BigDecimal calculateTax(BigDecimal amount) {
        // 5% GST for medicines
        return amount.multiply(BigDecimal.valueOf(0.05)).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private CartResponse toCartResponse(Cart cart) {
        return CartResponse.builder()
                .userId(cart.getUserId())
                .items(toCartItemResponses(cart.getItems()))
                .itemCount(cart.getItemCount())
                .totalItems(cart.getTotalItems())
                .subtotal(cart.getSubtotal())
                .couponCode(cart.getCouponCode())
                .discountAmount(cart.getDiscountAmount())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private List<CartItemResponse> toCartItemResponses(List<CartItem> items) {
        return items.stream()
                .map(item -> CartItemResponse.builder()
                        .itemId(item.getItemId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .manufacturer(item.getManufacturer())
                        .strength(item.getStrength())
                        .formulation(item.getFormulation())
                        .packSize(item.getPackSize())
                        .unitPrice(item.getUnitPrice())
                        .mrp(item.getMrp())
                        .quantity(item.getQuantity())
                        .total(item.getTotal())
                        .savings(item.getSavings())
                        .requiresPrescription(item.getRequiresPrescription())
                        .prescriptionId(item.getPrescriptionId())
                        .imageUrl(item.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }
}
