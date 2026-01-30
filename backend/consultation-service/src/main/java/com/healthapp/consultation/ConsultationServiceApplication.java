package com.healthapp.consultation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableR2dbcAuditing
@EnableScheduling
public class ConsultationServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ConsultationServiceApplication.class, args);
    }
}
