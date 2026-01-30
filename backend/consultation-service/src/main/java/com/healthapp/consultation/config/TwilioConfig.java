package com.healthapp.consultation.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Twilio SDK configuration for video calling.
 */
@Slf4j
@Getter
@Configuration
public class TwilioConfig {
    
    @Value("${twilio.account-sid}")
    private String accountSid;
    
    @Value("${twilio.api-key-sid}")
    private String apiKeySid;
    
    @Value("${twilio.api-key-secret}")
    private String apiKeySecret;
    
    @Value("${twilio.video.room-type:group}")
    private String roomType;
    
    @Value("${twilio.video.max-participants:2}")
    private int maxParticipants;
    
    @Value("${twilio.video.record-participants-on-connect:false}")
    private boolean recordParticipantsOnConnect;
    
    @Value("${twilio.video.status-callback-url:}")
    private String statusCallbackUrl;
    
    @Value("${consultation.token.ttl-seconds:3600}")
    private int tokenTtlSeconds;
    
    @PostConstruct
    public void initTwilio() {
        if (!accountSid.startsWith("test_")) {
            Twilio.init(accountSid, apiKeySecret);
            log.info("Twilio SDK initialized with account: {}", accountSid);
        } else {
            log.warn("Twilio running in test mode - video features will be simulated");
        }
    }
}
