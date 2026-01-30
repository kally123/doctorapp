package com.healthapp.payment.controller;

import com.healthapp.payment.domain.Refund;
import com.healthapp.payment.dto.*;
import com.healthapp.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/initiate")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PaymentInitiationResponse> initiatePayment(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody PaymentRequest request) {
        request = request.toBuilder().userId(UUID.fromString(userId)).build();
        return paymentService.initiatePayment(request);
    }
    
    @PostMapping("/verify")
    public Mono<PaymentVerificationResponse> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request) {
        return paymentService.verifyPaymentSignature(request)
                .flatMap(valid -> {
                    if (valid) {
                        return paymentService.completePayment(request)
                                .map(payment -> PaymentVerificationResponse.builder()
                                        .valid(true)
                                        .message("Payment verified and completed")
                                        .paymentId(payment.getId().toString())
                                        .status(payment.getStatus().name())
                                        .build());
                    } else {
                        return Mono.just(PaymentVerificationResponse.builder()
                                .valid(false)
                                .message("Invalid signature")
                                .build());
                    }
                });
    }
    
    @PostMapping("/webhook")
    public Mono<ResponseEntity<Void>> handleWebhook(
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestBody String payload) {
        return paymentService.verifyWebhookSignature(payload, signature)
                .filter(valid -> valid)
                .flatMap(valid -> paymentService.handleWebhook("payment.captured", payload))
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().<Void>build()));
    }
    
    @PostMapping("/{paymentId}/refund")
    public Mono<Refund> refundPayment(
            @PathVariable String paymentId,
            @Valid @RequestBody RefundRequest request) {
        request = request.toBuilder().paymentId(UUID.fromString(paymentId)).build();
        return paymentService.processRefund(request);
    }
    
    @GetMapping("/{paymentId}")
    public Mono<PaymentDto> getPayment(@PathVariable String paymentId) {
        return paymentService.getPayment(UUID.fromString(paymentId));
    }
    
    @GetMapping("/order/{orderType}/{orderId}")
    public Mono<PaymentDto> getPaymentByOrder(
            @PathVariable String orderType,
            @PathVariable String orderId) {
        return paymentService.getPaymentByOrder(orderType, UUID.fromString(orderId));
    }
    
    @GetMapping("/history")
    public Flux<PaymentDto> getPaymentHistory(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paymentService.getPaymentHistory(UUID.fromString(userId), page, size);
    }
}
