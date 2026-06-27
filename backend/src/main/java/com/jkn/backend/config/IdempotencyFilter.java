package com.jkn.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkn.backend.dto.ApiResponse;
import com.jkn.backend.entity.IdempotencyLog;
import com.jkn.backend.entity.IdempotencyStatus;
import com.jkn.backend.repository.IdempotencyLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        // Hanya apply pada endpoint POST (mutasi) antrean
        return !request.getRequestURI().startsWith("/api/queues") || !request.getMethod().equalsIgnoreCase("POST");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String idempotencyKey = request.getHeader(IDEMPOTENCY_HEADER);

        // Jika tidak ada header, teruskan request (meskipun di production Android wajib ngirim)
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<IdempotencyLog> existingLogOpt = idempotencyLogRepository.findById(idempotencyKey);

        if (existingLogOpt.isPresent()) {
            IdempotencyLog log = existingLogOpt.get();
            if (log.getStatus() == IdempotencyStatus.COMPLETED) {
                // Kembalikan response yang di-cache
                response.setStatus(HttpStatus.OK.value());
                response.setContentType("application/json");
                response.getWriter().write(log.getResponseBody());
                response.getWriter().flush();
                return;
            } else if (log.getStatus() == IdempotencyStatus.PROCESSING) {
                // Return 202 Accepted + polling hint
                response.setStatus(HttpStatus.ACCEPTED.value());
                response.setContentType("application/json");
                ApiResponse<Object> apiResponse = ApiResponse.error(HttpStatus.ACCEPTED.value(), "Request sedang diproses");
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                response.getWriter().flush();
                return;
            }
        }

        // Simpan state awal: PROCESSING (TTL 24 jam)
        IdempotencyLog newLog = new IdempotencyLog(
                idempotencyKey,
                IdempotencyStatus.PROCESSING,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(24)
        );
        idempotencyLogRepository.saveAndFlush(newLog);

        try {
            // Lanjutkan eksekusi controller. 
            // Update ke COMPLETED & simpan responseBody akan ditangani oleh QueueServiceImpl secara atomic.
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // Jika error dari layer controller/service, ubah menjadi FAILED agar bisa di-retry.
            newLog.setStatus(IdempotencyStatus.FAILED);
            idempotencyLogRepository.save(newLog);
            throw e;
        }
    }
}
