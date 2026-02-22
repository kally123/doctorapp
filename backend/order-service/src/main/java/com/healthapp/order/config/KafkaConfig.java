package com.healthapp.order.config;

import org.springframework.context.annotation.Profile;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka configuration for event publishing.
 */
@Profile("!test")
@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.order-events:order-events}")
    private String orderEventsTopic;

    @Value("${kafka.topics.lab-booking-events:lab-booking-events}")
    private String labBookingEventsTopic;

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(orderEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic labBookingEventsTopic() {
        return TopicBuilder.name(labBookingEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
