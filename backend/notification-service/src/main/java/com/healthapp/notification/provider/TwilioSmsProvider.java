package com.healthapp.notification.provider;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class TwilioSmsProvider implements SmsProvider {
    
    private final OkHttpClient httpClient;
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final boolean enabled;
    
    public TwilioSmsProvider(
            OkHttpClient httpClient,
            @Value("${notification.sms.twilio.account-sid:}") String accountSid,
            @Value("${notification.sms.twilio.auth-token:}") String authToken,
            @Value("${notification.sms.twilio.from-number:}") String fromNumber,
            @Value("${notification.sms.enabled:false}") boolean enabled) {
        this.httpClient = httpClient;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        this.enabled = enabled;
    }
    
    @Override
    public Mono<Boolean> sendSms(String phoneNumber, String message, Map<String, Object> metadata) {
        if (!enabled || accountSid.isEmpty()) {
            log.warn("SMS sending is disabled or not configured. Message to {} not sent.", phoneNumber);
            return Mono.just(true); // Return true in dev mode
        }
        
        return Mono.fromCallable(() -> {
            try {
                String url = String.format(
                    "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json",
                    accountSid
                );
                
                RequestBody formBody = new FormBody.Builder()
                        .add("To", phoneNumber)
                        .add("From", fromNumber)
                        .add("Body", message)
                        .build();
                
                String credential = Credentials.basic(accountSid, authToken);
                
                Request request = new Request.Builder()
                        .url(url)
                        .post(formBody)
                        .header("Authorization", credential)
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        log.info("SMS sent successfully to: {}", phoneNumber);
                        return true;
                    } else {
                        log.error("Failed to send SMS. Status: {}, Body: {}", 
                                response.code(), 
                                response.body() != null ? response.body().string() : "null");
                        return false;
                    }
                }
            } catch (IOException e) {
                log.error("Failed to send SMS to: {}", phoneNumber, e);
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Mono<Boolean> sendOtp(String phoneNumber, String otp, String templateId) {
        String message = String.format("Your OTP is: %s. Valid for 5 minutes.", otp);
        return sendSms(phoneNumber, message, null);
    }
    
    @Override
    public String getProviderName() {
        return "Twilio";
    }
}
