package com.healthapp.notification.provider;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface SmsProvider {
    
    Mono<Boolean> sendSms(String phoneNumber, String message, Map<String, Object> metadata);
    
    Mono<Boolean> sendOtp(String phoneNumber, String otp, String templateId);
    
    String getProviderName();
}
