package com.jkn.backend.controller;

import com.jkn.backend.service.HealthCheckService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health Check Controller (TASK-07-C).
 *
 * Endpoint:
 * - GET /health       → Full health report (200 healthy/degraded, 503 unhealthy)
 * - GET /health/ready → Kubernetes readiness probe
 * - GET /health/live  → Kubernetes liveness probe
 */
@RestController
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    public HealthCheckController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = healthCheckService.getFullHealth();
        String status = (String) result.get("status");

        if ("unhealthy".equals(status)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, String>> readiness() {
        if (healthCheckService.isReady()) {
            return ResponseEntity.ok(Map.of("status", "ready"));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("status", "not_ready"));
    }

    @GetMapping("/health/live")
    public ResponseEntity<Map<String, String>> liveness() {
        if (healthCheckService.isAlive()) {
            return ResponseEntity.ok(Map.of("status", "alive"));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("status", "dead"));
    }
}
