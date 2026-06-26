package com.jkn.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet Filter untuk Structured Logging (TASK-07-A).
 *
 * Fungsi:
 * 1. Generate/Extract X-Request-ID dari header incoming request.
 * 2. Inject request_id, endpoint, user_id ke SLF4J MDC.
 * 3. Set X-Request-ID di response header untuk client correlation.
 * 4. Log INFO saat request masuk & keluar, termasuk duration_ms dan status_code.
 * 5. PII Protection: TIDAK log body request (bisa mengandung NIK/telepon).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        // 1. Extract or generate X-Request-ID
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // 2. Inject ke MDC (otomatis masuk ke semua log dalam request ini)
        MDC.put("request_id", requestId);
        MDC.put("endpoint", request.getMethod() + " " + request.getRequestURI());
        // user_id bisa di-set oleh layer auth di masa depan
        MDC.put("user_id", request.getHeader("X-User-ID") != null
                ? request.getHeader("X-User-ID") : "anonymous");

        // 3. Set response header
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            // Log request masuk
            log.info("Request received: {} {}", request.getMethod(), request.getRequestURI());

            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("duration_ms", String.valueOf(duration));

            // Log request selesai
            log.info("Request completed: {} {} — status={} duration={}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration);

            // 4. Bersihkan MDC
            MDC.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Jangan filter endpoint actuator dan static resources
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/ws-queue");
    }
}
