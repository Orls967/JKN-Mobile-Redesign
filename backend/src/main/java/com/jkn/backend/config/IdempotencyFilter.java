package com.jkn.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkn.backend.dto.ErrorResponse;
import com.jkn.backend.entity.IdempotencyLog;
import com.jkn.backend.entity.IdempotencyStatus;
import com.jkn.backend.repository.IdempotencyLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";

    private final IdempotencyLogRepository idempotencyLogRepository;
    private final ObjectMapper objectMapper;

    public IdempotencyFilter(IdempotencyLogRepository idempotencyLogRepository, ObjectMapper objectMapper) {
        this.idempotencyLogRepository = idempotencyLogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/queues")
                || !request.getMethod().equalsIgnoreCase("POST");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String idempotencyKey = request.getHeader(IDEMPOTENCY_HEADER);

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println(
            "FILTER START : "
            + Thread.currentThread().getName()
            + " -> "
            + idempotencyKey
        );

        Optional<IdempotencyLog> existingLogOpt = idempotencyLogRepository.findById(idempotencyKey);

        if (existingLogOpt.isPresent()) {
            IdempotencyLog log = existingLogOpt.get();
            if (log.getStatus() == IdempotencyStatus.COMPLETED) {
                response.setStatus(HttpStatus.OK.value());
                response.setContentType("application/json");
                response.getWriter().write(log.getResponseBody());
                response.getWriter().flush();
                return;
            } else if (log.getStatus() == IdempotencyStatus.PROCESSING) {
                response.setStatus(HttpStatus.CONFLICT.value());
                response.setContentType("application/json");

                Object reqIdObj = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
                String requestId = reqIdObj != null ? reqIdObj.toString() : "unknown";

                ErrorResponse errorBody = new ErrorResponse(
                        "QUEUE_IN_PROGRESS",
                        "Request sedang diproses",
                        true,
                        2,
                        requestId
                );

                response.getWriter().write(objectMapper.writeValueAsString(errorBody));
                response.getWriter().flush();
                return;
            }
        }


        IdempotencyLog newLog = new IdempotencyLog(
                idempotencyKey,
                IdempotencyStatus.PROCESSING,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(24)
        );

        System.out.println("========== SAVE NEW LOG ==========");
        System.out.println("Thread = " + Thread.currentThread().getName());
        System.out.println("Key    = " + idempotencyKey);
        
        try {
            idempotencyLogRepository.saveAndFlush(newLog);
            System.out.println(
                "INSERT PROCESSING : "
                + Thread.currentThread().getName()
            );

        } catch (DataIntegrityViolationException ex) {  
            System.out.println("========== DUPLICATE ==========");
            System.out.println("Thread = " + Thread.currentThread().getName());

            IdempotencyLog existing = idempotencyLogRepository.findById(idempotencyKey).orElseThrow();

            if (existing.getStatus() == IdempotencyStatus.PROCESSING) {
                response.setStatus(HttpStatus.CONFLICT.value());
                response.setContentType("application/json");

                Object reqIdObj = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
                String requestId = reqIdObj != null ? reqIdObj.toString() : "unknown";

                ErrorResponse errorBody = new ErrorResponse(
                        "QUEUE_IN_PROGRESS",
                        "Request sedang diproses",
                        true,
                        2,
                        requestId
                );

                response.getWriter().write(objectMapper.writeValueAsString(errorBody));
                response.getWriter().flush();
                return;
            }

            if (existing.getStatus() == IdempotencyStatus.COMPLETED) {
                response.setStatus(HttpStatus.OK.value());
                response.setContentType("application/json");
                response.getWriter().write(existing.getResponseBody());
                response.getWriter().flush();
                return;
            }

            throw ex;
        }

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            newLog.setStatus(IdempotencyStatus.FAILED);
            idempotencyLogRepository.save(newLog);
            throw e;
        }
    }
}
