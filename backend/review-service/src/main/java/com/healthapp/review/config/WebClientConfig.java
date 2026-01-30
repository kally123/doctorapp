package com.healthapp.review.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.doctor-service.url}")
    private String doctorServiceUrl;

    @Value("${services.consultation-service.url}")
    private String consultationServiceUrl;

    @Value("${services.search-service.url}")
    private String searchServiceUrl;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient doctorServiceClient(WebClient.Builder builder) {
        return builder
                .baseUrl(doctorServiceUrl)
                .build();
    }

    @Bean
    public WebClient consultationServiceClient(WebClient.Builder builder) {
        return builder
                .baseUrl(consultationServiceUrl)
                .build();
    }

    @Bean
    public WebClient searchServiceClient(WebClient.Builder builder) {
        return builder
                .baseUrl(searchServiceUrl)
                .build();
    }
}
