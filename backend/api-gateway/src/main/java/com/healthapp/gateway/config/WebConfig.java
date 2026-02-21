package com.healthapp.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static files from /static/** path
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // Serve health-dashboard.html from root and /tic/ paths
        registry.addResourceHandler("/health-dashboard.html", "/tic/health-dashboard.html")
                .addResourceLocations("classpath:/static/");
    }
}
