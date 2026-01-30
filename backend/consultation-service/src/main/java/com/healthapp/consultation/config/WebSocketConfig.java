package com.healthapp.consultation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration with STOMP for real-time chat.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker for subscriptions
        // /topic - for broadcast messages to all subscribers
        // /queue - for point-to-point messages
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages FROM client TO server
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Main WebSocket endpoint
        registry.addEndpoint("/ws/consultation")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Fallback for older browsers
        
        // Native WebSocket endpoint (without SockJS)
        registry.addEndpoint("/ws/consultation")
                .setAllowedOriginPatterns("*");
    }
}
