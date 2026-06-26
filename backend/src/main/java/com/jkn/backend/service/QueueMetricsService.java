package com.jkn.backend.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom Metrics Service (TASK-07-B).
 *
 * Mendaftarkan dan mengelola semua custom metrics yang di-expose ke Prometheus.
 * Metrics untuk DB pool (HikariCP) dan JVM runtime sudah otomatis dari Micrometer.
 */
@Service
public class QueueMetricsService {

    // --- Business Metrics ---
    private final Counter queueRegistrationSuccess;
    private final Counter queueRegistrationFailed;
    private final Counter queueRegistrationDuplicate;
    private final Timer queueRegistrationDuration;

    private final Counter queueNextSuccess;
    private final Counter queueNextFailed;
    private final Timer queueNextDuration;

    // --- Reliability Metrics (Placeholder — diisi oleh Story lain) ---
    private final Counter rateLimitRejected;
    private final Counter idempotencyCacheHit;
    private final Counter idempotencyCacheMiss;

    public QueueMetricsService(MeterRegistry registry) {
        // Business metrics
        this.queueRegistrationSuccess = Counter.builder("queue_registration_total")
                .tag("status", "success")
                .description("Total successful queue registrations")
                .register(registry);

        this.queueRegistrationFailed = Counter.builder("queue_registration_total")
                .tag("status", "failed")
                .description("Total failed queue registrations")
                .register(registry);

        this.queueRegistrationDuplicate = Counter.builder("queue_registration_total")
                .tag("status", "duplicate")
                .description("Total duplicate queue registrations")
                .register(registry);

        this.queueRegistrationDuration = Timer.builder("queue_registration_duration_seconds")
                .description("Duration of queue registration operations")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        this.queueNextSuccess = Counter.builder("queue_next_total")
                .tag("status", "success")
                .description("Total successful queue NEXT operations")
                .register(registry);

        this.queueNextFailed = Counter.builder("queue_next_total")
                .tag("status", "failed")
                .description("Total failed queue NEXT operations")
                .register(registry);

        this.queueNextDuration = Timer.builder("queue_next_duration_seconds")
                .description("Duration of queue NEXT operations")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        // Placeholder metrics — akan diisi oleh Story lain (Circuit Breaker, Rate Limiter, Idempotency)
        registry.gauge("circuit_breaker_state",
                io.micrometer.core.instrument.Tags.of("dependency", "bpjs-external"),
                new AtomicInteger(0)); // 0=CLOSED

        this.rateLimitRejected = Counter.builder("rate_limit_rejected_total")
                .tag("endpoint", "/queue/register")
                .description("Total rate-limited requests (placeholder)")
                .register(registry);

        this.idempotencyCacheHit = Counter.builder("idempotency_cache_hit_total")
                .description("Total idempotency cache hits (placeholder)")
                .register(registry);

        this.idempotencyCacheMiss = Counter.builder("idempotency_cache_miss_total")
                .description("Total idempotency cache misses (placeholder)")
                .register(registry);
    }

    // --- Public API untuk instrumentasi ---

    public void recordRegistrationSuccess() {
        queueRegistrationSuccess.increment();
    }

    public void recordRegistrationFailed() {
        queueRegistrationFailed.increment();
    }

    public Timer getRegistrationTimer() {
        return queueRegistrationDuration;
    }

    public void recordNextSuccess() {
        queueNextSuccess.increment();
    }

    public void recordNextFailed() {
        queueNextFailed.increment();
    }

    public Timer getNextTimer() {
        return queueNextDuration;
    }
}
