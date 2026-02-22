package com.healthapp.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Order Service Application - Commerce module for medicine orders and lab test bookings.
 * 
 * Features:
 * - Shopping cart with Redis storage
 * - Medicine ordering from prescriptions
 * - Lab test catalog and booking with home collection
 * - Real-time order tracking via WebSocket
 * - Partner (pharmacy/lab) management
 * - EHR integration for lab reports
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
