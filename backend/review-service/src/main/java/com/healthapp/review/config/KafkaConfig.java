package com.healthapp.review.config;

import org.springframework.context.annotation.Profile;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Profile("!test")
@Configuration
public class KafkaConfig {

    public static final String REVIEW_EVENTS_TOPIC = "review-events";
    public static final String RATING_UPDATE_TOPIC = "rating-update-events";

    @Bean
    public NewTopic reviewEventsTopic() {
        return TopicBuilder.name(REVIEW_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ratingUpdateTopic() {
        return TopicBuilder.name(RATING_UPDATE_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
