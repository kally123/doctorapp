package com.healthapp.notification.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FcmPushProvider implements PushProvider {
    
    private static final String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String serverKey;
    private final boolean enabled;
    
    public FcmPushProvider(
            OkHttpClient httpClient,
            ObjectMapper objectMapper,
            @Value("${notification.push.fcm.server-key:}") String serverKey,
            @Value("${notification.push.enabled:false}") boolean enabled) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.serverKey = serverKey;
        this.enabled = enabled;
    }
    
    @Override
    public Mono<Boolean> sendPush(String deviceToken, String title, String body, Map<String, String> data) {
        if (!enabled || serverKey.isEmpty()) {
            log.warn("Push notifications disabled or not configured. Message to device not sent.");
            return Mono.just(true); // Return true in dev mode
        }
        
        return Mono.fromCallable(() -> {
            try {
                Map<String, Object> payload = buildPayload(deviceToken, title, body, data);
                String jsonPayload = objectMapper.writeValueAsString(payload);
                
                RequestBody requestBody = RequestBody.create(jsonPayload, JSON);
                
                Request request = new Request.Builder()
                        .url(FCM_API_URL)
                        .post(requestBody)
                        .header("Authorization", "key=" + serverKey)
                        .header("Content-Type", "application/json")
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        log.info("Push notification sent successfully to device: {}", 
                                deviceToken.substring(0, Math.min(10, deviceToken.length())) + "...");
                        return true;
                    } else {
                        log.error("Failed to send push notification. Status: {}, Body: {}", 
                                response.code(), 
                                response.body() != null ? response.body().string() : "null");
                        return false;
                    }
                }
            } catch (IOException e) {
                log.error("Failed to send push notification", e);
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Mono<Integer> sendPushToMultiple(List<String> deviceTokens, String title, String body, Map<String, String> data) {
        if (!enabled || serverKey.isEmpty()) {
            log.warn("Push notifications disabled or not configured.");
            return Mono.just(deviceTokens.size());
        }
        
        return Mono.fromCallable(() -> {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("registration_ids", deviceTokens);
                
                Map<String, Object> notification = new HashMap<>();
                notification.put("title", title);
                notification.put("body", body);
                notification.put("sound", "default");
                payload.put("notification", notification);
                
                if (data != null && !data.isEmpty()) {
                    payload.put("data", data);
                }
                
                String jsonPayload = objectMapper.writeValueAsString(payload);
                
                RequestBody requestBody = RequestBody.create(jsonPayload, JSON);
                
                Request request = new Request.Builder()
                        .url(FCM_API_URL)
                        .post(requestBody)
                        .header("Authorization", "key=" + serverKey)
                        .header("Content-Type", "application/json")
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        log.info("Push notifications sent to {} devices", deviceTokens.size());
                        return deviceTokens.size();
                    } else {
                        log.error("Failed to send push notifications. Status: {}", response.code());
                        return 0;
                    }
                }
            } catch (IOException e) {
                log.error("Failed to send push notifications", e);
                return 0;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    private Map<String, Object> buildPayload(String deviceToken, String title, String body, Map<String, String> data) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("to", deviceToken);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("body", body);
        notification.put("sound", "default");
        notification.put("click_action", "OPEN_APP");
        payload.put("notification", notification);
        
        if (data != null && !data.isEmpty()) {
            payload.put("data", data);
        }
        
        // High priority for important notifications
        payload.put("priority", "high");
        
        return payload;
    }
    
    @Override
    public String getProviderName() {
        return "FCM";
    }
}
