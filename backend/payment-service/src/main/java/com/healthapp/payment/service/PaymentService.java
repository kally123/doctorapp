package com.healthapp.payment.service;

import com.healthapp.payment.domain.PaymentStatus;
import com.healthapp.payment.domain.PaymentTransaction;
import com.healthapp.payment.domain.Refund;
import com.healthapp.payment.dto.*;
import com.healthapp.payment.event.PaymentEventPublisher;
import com.healthapp.payment.gateway.RazorpayGateway;
import com.healthapp.payment.repository.PaymentRepository;
import com.healthapp.payment.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepo;
    private final RefundRepository refundRepo;
    private final RazorpayGateway razorpayGateway;
    private final PaymentEventPublisher eventPublisher;
    
    @Value("${payment.razorpay.key-id}")
    private String razorpayKeyId;
    
    @Value("${payment.razorpay.key-secret}")
    private String razorpayKeySecret;
    
    public Mono<PaymentInitiationResponse> initiatePayment(PaymentRequest request) {
        // Check for idempotency
        if (request.getIdempotencyKey() != null) {
            return paymentRepo.findByIdempotencyKey(request.getIdempotencyKey())
                    .flatMap(existing -> Mono.just(toInitiationResponse(existing)))
                    .switchIfEmpty(createNewPayment(request));
        }
        
        return createNewPayment(request);
    }
    
    private Mono<PaymentInitiationResponse> createNewPayment(PaymentRequest request) {
        String currency = request.getCurrency() != null ? request.getCurrency() : "INR";
        
        // Create Razorpay order
        return razorpayGateway.createOrder(request.getAmount(), currency, request.getOrderId().toString())
                .flatMap(razorpayOrderId -> {
                    PaymentTransaction payment = PaymentTransaction.builder()
                            .orderType(request.getOrderType())
                            .orderId(request.getOrderId())
                            .userId(request.getUserId())
                            .amount(request.getAmount())
                            .currency(currency)
                            .status(PaymentStatus.INITIATED)
                            .gateway("RAZORPAY")
                            .gatewayOrderId(razorpayOrderId)
                            .description(request.getDescription())
                            .idempotencyKey(request.getIdempotencyKey() != null ? 
                                    request.getIdempotencyKey() : UUID.randomUUID().toString())
                            .refundedAmount(BigDecimal.ZERO)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    
                    return paymentRepo.save(payment);
                })
                .doOnSuccess(payment -> log.info("Payment initiated: {}", payment.getId()))
                .map(this::toInitiationResponse);
    }
    
    public Mono<Boolean> verifyPaymentSignature(PaymentVerificationRequest request) {
        return Mono.fromCallable(() -> {
            String payload = request.getGatewayOrderId() + "|" + request.getGatewayPaymentId();
            String expectedSignature = new HmacUtils("HmacSHA256", razorpayKeySecret)
                    .hmacHex(payload);
            return expectedSignature.equals(request.getGatewaySignature());
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    public Mono<PaymentTransaction> completePayment(PaymentVerificationRequest request) {
        return verifyPaymentSignature(request)
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new RuntimeException("Invalid payment signature"));
                    }
                    
                    return paymentRepo.findByGatewayOrderId(request.getGatewayOrderId())
                            .switchIfEmpty(Mono.error(new RuntimeException("Payment not found")))
                            .flatMap(payment -> {
                                PaymentTransaction updated = payment.toBuilder()
                                        .status(PaymentStatus.COMPLETED)
                                        .gatewayPaymentId(request.getGatewayPaymentId())
                                        .gatewaySignature(request.getGatewaySignature())
                                        .completedAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();
                                
                                return paymentRepo.save(updated);
                            })
                            .doOnSuccess(payment -> eventPublisher.publishPaymentCompleted(payment));
                });
    }
    
    public Mono<PaymentTransaction> handleWebhook(String eventType, String payload) {
        return Mono.defer(() -> {
            switch (eventType) {
                case "payment.captured":
                    return handlePaymentCaptured(payload);
                case "payment.failed":
                    return handlePaymentFailed(payload);
                case "refund.processed":
                    return handleRefundProcessed(payload);
                default:
                    log.warn("Unhandled webhook event: {}", eventType);
                    return Mono.empty();
            }
        });
    }
    
    private Mono<PaymentTransaction> handlePaymentCaptured(String payload) {
        // Parse payload and update payment status
        // This is a simplified implementation
        return Mono.empty();
    }
    
    private Mono<PaymentTransaction> handlePaymentFailed(String payload) {
        return Mono.empty();
    }
    
    private Mono<PaymentTransaction> handleRefundProcessed(String payload) {
        return Mono.empty();
    }
    
    public Mono<Boolean> verifyWebhookSignature(String payload, String signature) {
        return Mono.fromCallable(() -> {
            String expectedSignature = new HmacUtils("HmacSHA256", razorpayKeySecret)
                    .hmacHex(payload);
            return expectedSignature.equals(signature);
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    public Mono<Refund> processRefund(RefundRequest request) {
        return paymentRepo.findById(request.getPaymentId())
                .switchIfEmpty(Mono.error(new RuntimeException("Payment not found")))
                .flatMap(payment -> {
                    if (payment.getStatus() != PaymentStatus.COMPLETED) {
                        return Mono.error(new RuntimeException("Payment not eligible for refund"));
                    }
                    
                    BigDecimal refundableAmount = payment.getAmount()
                            .subtract(payment.getRefundedAmount());
                    
                    if (request.getAmount().compareTo(refundableAmount) > 0) {
                        return Mono.error(new RuntimeException("Refund amount exceeds refundable amount"));
                    }
                    
                    return createRefund(payment, request);
                });
    }
    
    private Mono<Refund> createRefund(PaymentTransaction payment, RefundRequest request) {
        return razorpayGateway.createRefund(payment.getGatewayPaymentId(), request.getAmount())
                .flatMap(gatewayRefundId -> {
                    Refund refund = Refund.builder()
                            .paymentId(payment.getId())
                            .amount(request.getAmount())
                            .currency(payment.getCurrency())
                            .reason(request.getReason())
                            .status("PROCESSING")
                            .gatewayRefundId(gatewayRefundId)
                            .createdAt(Instant.now())
                            .build();
                    
                    return refundRepo.save(refund);
                })
                .flatMap(refund -> {
                    // Update payment's refunded amount
                    BigDecimal newRefundedAmount = payment.getRefundedAmount().add(request.getAmount());
                    PaymentStatus newStatus = newRefundedAmount.compareTo(payment.getAmount()) >= 0
                            ? PaymentStatus.REFUNDED
                            : PaymentStatus.PARTIALLY_REFUNDED;
                    
                    PaymentTransaction updated = payment.toBuilder()
                            .refundedAmount(newRefundedAmount)
                            .status(newStatus)
                            .updatedAt(Instant.now())
                            .build();
                    
                    return paymentRepo.save(updated).thenReturn(refund);
                })
                .doOnSuccess(refund -> eventPublisher.publishRefundProcessed(refund));
    }
    
    public Flux<PaymentDto> getPaymentHistory(UUID userId, int page, int size) {
        int offset = page * size;
        return paymentRepo.findByUserIdPaginated(userId, size, offset)
                .map(this::toDto);
    }
    
    public Mono<PaymentDto> getPayment(UUID paymentId) {
        return paymentRepo.findById(paymentId)
                .map(this::toDto);
    }
    
    public Mono<PaymentDto> getPaymentByOrder(String orderType, UUID orderId) {
        return paymentRepo.findByOrderTypeAndOrderId(orderType, orderId)
                .map(this::toDto);
    }
    
    private PaymentInitiationResponse toInitiationResponse(PaymentTransaction payment) {
        return PaymentInitiationResponse.builder()
                .paymentId(payment.getId())
                .gatewayOrderId(payment.getGatewayOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .razorpayKeyId(razorpayKeyId)
                .prefill(Map.of())
                .build();
    }
    
    private PaymentDto toDto(PaymentTransaction payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderType(payment.getOrderType())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .gateway(payment.getGateway())
                .gatewayOrderId(payment.getGatewayOrderId())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .failureReason(payment.getFailureReason())
                .refundedAmount(payment.getRefundedAmount())
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}
