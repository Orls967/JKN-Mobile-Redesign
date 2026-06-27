package com.jkn.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JknBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JknBackendApplication.class, args);
    }
}
