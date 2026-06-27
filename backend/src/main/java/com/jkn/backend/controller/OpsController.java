package com.jkn.backend.controller;

import com.jkn.backend.dto.CircuitBreakerResetResponse;
import com.jkn.backend.dto.CircuitBreakerStatusResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/ops/circuit-breaker")
public class OpsController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public OpsController(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @GetMapping("/status")
    public ResponseEntity<CircuitBreakerStatusResponse> getStatus() {
        List<CircuitBreakerStatusResponse.CircuitBreakerInfo> circuits = new ArrayList<>();
        
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            String name = cb.getName();
            String state = cb.getState().name();
            double failureRate = cb.getMetrics().getFailureRate();
            circuits.add(new CircuitBreakerStatusResponse.CircuitBreakerInfo(name, state, failureRate));
        });
        
        return ResponseEntity.ok(new CircuitBreakerStatusResponse(circuits));
    }

    @PostMapping("/{dependency}/reset")
    public ResponseEntity<CircuitBreakerResetResponse> resetDependency(@PathVariable String dependency) {
        try {
            CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(dependency);
            String prevState = cb.getState().name();
            
            // Force transition to HALF_OPEN to probe next 3 requests
            cb.transitionToHalfOpenState();
            
            CircuitBreakerResetResponse response = new CircuitBreakerResetResponse(
                    dependency,
                    prevState,
                    "HALF_OPEN",
                    "Circuit reset to HALF_OPEN. Will probe next 3 requests."
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new CircuitBreakerResetResponse(
                    dependency, "UNKNOWN", "UNKNOWN", "Dependency tidak ditemukan atau gagal di-reset."
            ));
        }
    }
}
