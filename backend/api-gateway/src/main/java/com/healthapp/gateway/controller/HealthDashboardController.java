package com.healthapp.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class HealthDashboardController {

    private static final String STATUS_KEY = "status";
    private static final String RESPONSE_TIME_KEY = "responseTime";
    private static final String URL_KEY = "url";
    private static final String NAME_KEY = "name";
    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";

    @Value("${USER_SERVICE_URL:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${DOCTOR_SERVICE_URL:http://localhost:8082}")
    private String doctorServiceUrl;

    @Value("${SEARCH_SERVICE_URL:http://localhost:8083}")
    private String searchServiceUrl;

    @Value("${APPOINTMENT_SERVICE_URL:http://localhost:8084}")
    private String appointmentServiceUrl;

    @Value("${PAYMENT_SERVICE_URL:http://localhost:8085}")
    private String paymentServiceUrl;

    @Value("${NOTIFICATION_SERVICE_URL:http://localhost:8086}")
    private String notificationServiceUrl;

    @Value("${CONSULTATION_SERVICE_URL:http://localhost:8087}")
    private String consultationServiceUrl;

    @Value("${PRESCRIPTION_SERVICE_URL:http://localhost:8088}")
    private String prescriptionServiceUrl;

    @Value("${EHR_SERVICE_URL:http://localhost:8089}")
    private String ehrServiceUrl;

    @Value("${ORDER_SERVICE_URL:http://localhost:8090}")
    private String orderServiceUrl;

    @Value("${REVIEW_SERVICE_URL:http://localhost:8091}")
    private String reviewServiceUrl;


    private final WebClient webClient;

    public HealthDashboardController() {
        this.webClient = WebClient.builder()
                .build();
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> dashboard() {
        return Mono.just("redirect:/health-dashboard.html");
    }

    @GetMapping(value = "/html", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<Resource> dashboardHtml() {
        return Mono.just(new ClassPathResource("static/health-dashboard.html"));
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<Map<String, Object>> getAllServicesHealth() {
        List<ServiceHealth> services = List.of(
                new ServiceHealth("API Gateway", "http://localhost:8080", true),
                new ServiceHealth("User Service", userServiceUrl, false),
                new ServiceHealth("Doctor Service", doctorServiceUrl, false),
                new ServiceHealth("Search Service", searchServiceUrl, false),
                new ServiceHealth("Appointment Service", appointmentServiceUrl, false),
                new ServiceHealth("Payment Service", paymentServiceUrl, false),
                new ServiceHealth("Notification Service", notificationServiceUrl, false),
                new ServiceHealth("Consultation Service", consultationServiceUrl, false),
                new ServiceHealth("Prescription Service", prescriptionServiceUrl, false),
                new ServiceHealth("EHR Service", ehrServiceUrl, false),
                new ServiceHealth("Order Service", orderServiceUrl, false),
                new ServiceHealth("Review Service", reviewServiceUrl, false)
        );

        List<Mono<Map<String, Object>>> healthChecks = services.stream()
                .map(this::checkServiceHealth)
                .toList();

        return Mono.zip(healthChecks, results -> {
            List<Map<String, Object>> serviceStatuses = new ArrayList<>();
            for (Object result : results) {
                @SuppressWarnings("unchecked")
                Map<String, Object> statusMap = (Map<String, Object>) result;
                serviceStatuses.add(statusMap);
            }

            long healthyCount = serviceStatuses.stream()
                    .filter(s -> STATUS_UP.equals(s.get(STATUS_KEY)))
                    .count();

            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", System.currentTimeMillis());
            response.put("totalServices", services.size());
            response.put("healthyServices", healthyCount);
            response.put("unhealthyServices", services.size() - healthyCount);
            response.put("overallStatus", healthyCount == services.size() ? "HEALTHY" : "DEGRADED");
            response.put("services", serviceStatuses);

            return response;
        });
    }

    private Mono<Map<String, Object>> checkServiceHealth(ServiceHealth service) {
        if (service.isSelf) {
            Map<String, Object> health = new HashMap<>();
            health.put(NAME_KEY, service.name);
            health.put(URL_KEY, service.url);
            health.put(STATUS_KEY, STATUS_UP);
            health.put(RESPONSE_TIME_KEY, 0);
            return Mono.just(health);
        }

        long startTime = System.currentTimeMillis();

        return webClient.get()
                .uri(service.url + "/actuator/health")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .map(response -> {
                    Map<String, Object> health = new HashMap<>();
                    health.put(NAME_KEY, service.name);
                    health.put(URL_KEY, service.url);
                    health.put(STATUS_KEY, response.getOrDefault(STATUS_KEY, "UNKNOWN"));
                    health.put(RESPONSE_TIME_KEY, System.currentTimeMillis() - startTime);
                    health.put("details", response.get("components"));
                    return health;
                })
                .onErrorResume(error -> {
                    Map<String, Object> health = new HashMap<>();
                    health.put(NAME_KEY, service.name);
                    health.put(URL_KEY, service.url);
                    health.put(STATUS_KEY, STATUS_DOWN);
                    health.put(RESPONSE_TIME_KEY, System.currentTimeMillis() - startTime);
                    health.put("error", error.getMessage());
                    return Mono.just(health);
                });
    }

    private record ServiceHealth(String name, String url, boolean isSelf) {}
}
