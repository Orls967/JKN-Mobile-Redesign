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
        String path = request.getRequestURI();
        String method = request.getMethod();
        // Hanya limit POST /api/queues dan GET /api/queues/status
        boolean isPostQueue = "POST".equalsIgnoreCase(method) && "/api/queues".equals(path);
        boolean isGetStatus = "GET".equalsIgnoreCase(method) && path.startsWith("/api/queues/status");
        return !(isPostQueue || isGetStatus);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        int limit = rateLimitConfig.getLimit(); // default POST 5/60s
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/queues/status")) {
            limit = 10; // TASK-03-B: Rate limit pada endpoint ini: max 10 request/menit
        }

        String identifier = request.getHeader("X-User-Id");
        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = request.getRemoteAddr();
        }
        
        // Scope limit per endpoint
        String limitKey = path + ":" + identifier;

        long now = Instant.now().toEpochMilli();
        long windowStart = now - (rateLimitConfig.getWindowSeconds() * 1000L);

        Queue<Long> requests = userRequests.computeIfAbsent(limitKey, k -> new ConcurrentLinkedQueue<>());

        while (!requests.isEmpty() && requests.peek() < windowStart) {
            requests.poll();
        }

        int currentRequests = requests.size();

        if (currentRequests >= limit) {
            long oldestRequestTime = requests.peek();
            long retryAfterMillis = (oldestRequestTime + (rateLimitConfig.getWindowSeconds() * 1000L)) - now;
            long retryAfterSeconds = (retryAfterMillis / 1000) + 1;

            log.warn("Rate limit exceeded for key: {}. Window limit: {}/{}s", limitKey, limit, rateLimitConfig.getWindowSeconds());

            response.setStatus(429);
            response.setContentType("application/json");

            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", String.valueOf((oldestRequestTime + (rateLimitConfig.getWindowSeconds() * 1000L)) / 1000));
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

            // Use X-Request-ID from attribute
            Object reqIdObj = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
            String requestId = reqIdObj != null ? reqIdObj.toString() : "unknown";

            com.jkn.backend.dto.ErrorResponse errorBody = new com.jkn.backend.dto.ErrorResponse(
                    "RATE_LIMIT_EXCEEDED",
                    "Terlalu banyak request. Silakan coba lagi.",
                    true,
                    (int) retryAfterSeconds,
                    requestId
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorBody));
            return;
        }

        requests.offer(now);

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(limit - requests.size()));
        
        filterChain.doFilter(request, response);
    }
}
