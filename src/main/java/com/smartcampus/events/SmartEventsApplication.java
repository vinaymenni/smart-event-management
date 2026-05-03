package com.smartcampus.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartEventsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartEventsApplication.class, args);
    }
}
