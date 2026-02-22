package com.healthapp.ehr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
    R2dbcAutoConfiguration.class,
    R2dbcDataAutoConfiguration.class
})
@EnableScheduling
public class EhrServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EhrServiceApplication.class, args);
    }
}
