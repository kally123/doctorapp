package com.healthapp.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthapp.order.domain.Cart;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for cart storage and caching.
 */
@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Cart> cartRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {
        
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Cart> valueSerializer = 
            new Jackson2JsonRedisSerializer<>(objectMapper, Cart.class);

        RedisSerializationContext<String, Cart> context = 
            RedisSerializationContext.<String, Cart>newSerializationContext(keySerializer)
                .value(valueSerializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        StringRedisSerializer serializer = new StringRedisSerializer();

        RedisSerializationContext<String, String> context = 
            RedisSerializationContext.<String, String>newSerializationContext(serializer)
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
