package com.expense.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // tells to run as a Spring Boot app
public class ExpenseBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpenseBackendApplication.class, args); // this loads all configurations, controllers, services, Firebase config
    }
}
