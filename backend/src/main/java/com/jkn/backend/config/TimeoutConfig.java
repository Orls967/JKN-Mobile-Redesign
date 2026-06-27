package com.jkn.backend.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class TimeoutConfig {

    /**
     * TASK-02-B: External HTTP Call Timeout Matrix
     * Connect Timeout: 3 seconds
     * Read Timeout: 5 seconds
     * Total Timeout: 8 seconds
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }
}
