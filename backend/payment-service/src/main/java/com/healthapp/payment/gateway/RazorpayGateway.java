package com.healthapp.payment.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class RazorpayGateway {
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Value("${payment.razorpay.key-id}")
    private String keyId;
    
    @Value("${payment.razorpay.key-secret}")
    private String keySecret;
    
    @Value("${payment.razorpay.api-url}")
    private String apiUrl;
    
    public Mono<String> createOrder(BigDecimal amount, String currency, String receipt) {
        return Mono.fromCallable(() -> {
            String json = String.format(
                    "{\"amount\": %d, \"currency\": \"%s\", \"receipt\": \"%s\", \"payment_capture\": 1}",
                    amount.multiply(new BigDecimal(100)).intValue(), // Convert to paise
                    currency,
                    receipt
            );
            
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
            
            Request request = new Request.Builder()
                    .url(apiUrl + "/orders")
                    .post(body)
                    .addHeader("Authorization", getAuthHeader())
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Razorpay API error: " + response.code());
                }
                
                String responseBody = response.body().string();
                JsonNode node = objectMapper.readTree(responseBody);
                return node.get("id").asText();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    public Mono<String> createRefund(String paymentId, BigDecimal amount) {
        return Mono.fromCallable(() -> {
            String json = String.format(
                    "{\"amount\": %d, \"speed\": \"normal\"}",
                    amount.multiply(new BigDecimal(100)).intValue()
            );
            
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
            
            Request request = new Request.Builder()
                    .url(apiUrl + "/payments/" + paymentId + "/refund")
                    .post(body)
                    .addHeader("Authorization", getAuthHeader())
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Razorpay refund error: " + response.code());
                }
                
                String responseBody = response.body().string();
                JsonNode node = objectMapper.readTree(responseBody);
                return node.get("id").asText();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    public Mono<JsonNode> getPaymentDetails(String paymentId) {
        return Mono.fromCallable(() -> {
            Request request = new Request.Builder()
                    .url(apiUrl + "/payments/" + paymentId)
                    .get()
                    .addHeader("Authorization", getAuthHeader())
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Razorpay API error: " + response.code());
                }
                
                String responseBody = response.body().string();
                return objectMapper.readTree(responseBody);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    private String getAuthHeader() {
        String credentials = keyId + ":" + keySecret;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
