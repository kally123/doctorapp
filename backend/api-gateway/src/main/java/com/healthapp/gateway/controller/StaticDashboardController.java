package com.healthapp.gateway.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class StaticDashboardController {

    @GetMapping(value = "/health-dashboard.html", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<ResponseEntity<String>> healthDashboard() {
        try {
            ClassPathResource resource = new ClassPathResource("static/health-dashboard.html");
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);

            return Mono.just(ResponseEntity.ok()
                    .headers(headers)
                    .body(content));
        } catch (IOException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("<html><body><h1>Dashboard not found</h1><p>" + e.getMessage() + "</p></body></html>"));
        }
    }
}
