package com.mt564.processing.svc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Mt564DeltaTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(Mt564DeltaTrackerApplication.class, args);
    }
}
