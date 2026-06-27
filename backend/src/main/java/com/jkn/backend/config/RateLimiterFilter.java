package com.jkn.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Order(1) // Jalankan paling awal, sebelum IdempotencyFilter
public class RateLimiterFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterFilter.class);

    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper;
    
    // In-Memory Storage for Sliding Window
    // Key: Identifier (X-User-Id atau IP), Value: Queue of timestamps in epoch milliseconds
    private final Map<String, Queue<Long>> userRequests = new ConcurrentHashMap<>();

    public RateLimiterFilter(RateLimitConfig rateLimitConfig, ObjectMapper objectMapper) {
        this.rateLimitConfig = rateLimitConfig;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Hanya limit POST /api/queues
        String path = request.getRequestURI();
        return !("POST".equalsIgnoreCase(request.getMethod()) && "/api/queues".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String identifier = request.getHeader("X-User-Id");
        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = request.getRemoteAddr(); // Fallback ke IP jika header tidak ada
        }

        long now = Instant.now().toEpochMilli();
        long windowStart = now - (rateLimitConfig.getWindowSeconds() * 1000L);

        // Ambil atau buat queue untuk user ini
        Queue<Long> requests = userRequests.computeIfAbsent(identifier, k -> new ConcurrentLinkedQueue<>());

        // Hapus request yang sudah melewati window 60 detik (Sliding Window logic)
        while (!requests.isEmpty() && requests.peek() < windowStart) {
            requests.poll();
        }

        int currentRequests = requests.size();
        int limit = rateLimitConfig.getLimit();

        if (currentRequests >= limit) {
            // Hitung sisa waktu sampai request tertua expire (reset window)
            long oldestRequestTime = requests.peek();
            long retryAfterMillis = (oldestRequestTime + (rateLimitConfig.getWindowSeconds() * 1000L)) - now;
            long retryAfterSeconds = (retryAfterMillis / 1000) + 1;

            log.warn("Rate limit exceeded for user: {}. Window limit: {}/{}s", identifier, limit, rateLimitConfig.getWindowSeconds());

            // Set response HTTP 429
            response.setStatus(429); // 429 Too Many Requests
            response.setContentType("application/json");

            // Set standard rate limit headers
            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", String.valueOf((oldestRequestTime + (rateLimitConfig.getWindowSeconds() * 1000L)) / 1000));
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

            // Set structured JSON error response
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("error_code", "RATE_LIMIT_EXCEEDED");
            errorBody.put("retryable", true);
            errorBody.put("retry_after", retryAfterSeconds);

            response.getWriter().write(objectMapper.writeValueAsString(errorBody));
            return; // Hentikan eksekusi chain
        }

        // Tambahkan request baru ke dalam window
        requests.offer(now);

        // Lanjutkan request & set header normal
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(limit - requests.size()));
        
        filterChain.doFilter(request, response);
    }
}
