package com.healthapp.notification.provider;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface PushProvider {
    
    Mono<Boolean> sendPush(String deviceToken, String title, String body, Map<String, String> data);
    
    Mono<Integer> sendPushToMultiple(List<String> deviceTokens, String title, String body, Map<String, String> data);
    
    String getProviderName();
}
