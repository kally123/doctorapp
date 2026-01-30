package com.healthapp.notification.provider;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface EmailProvider {
    
    Mono<Boolean> sendEmail(String to, String subject, String htmlBody, Map<String, Object> metadata);
    
    Mono<Boolean> sendEmailWithTemplate(String to, String templateId, Map<String, Object> variables);
    
    String getProviderName();
}
