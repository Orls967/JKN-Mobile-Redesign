package com.jkn.backend.controller;

import com.jkn.backend.dto.ApiResponse;
import com.jkn.backend.dto.CreateQueueRequest;
import com.jkn.backend.dto.QueueResponse;
import com.jkn.backend.service.QueueService;
import com.jkn.backend.service.QueueEtaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final QueueService queueService;
    private final QueueEtaService queueEtaService;

    public QueueController(QueueService queueService, QueueEtaService queueEtaService) {
        this.queueService = queueService;
        this.queueEtaService = queueEtaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QueueResponse>> createQueue(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CreateQueueRequest request) {
        QueueResponse response = queueService.createQueue(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QueueResponse>>> getAllQueues() {
        List<QueueResponse> responses = queueService.getAllQueues();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QueueResponse>> getQueueById(@PathVariable Long id) {
        QueueResponse response = queueService.getQueueById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/eta")
    public ResponseEntity<ApiResponse<com.jkn.backend.dto.EtaResponse>> getQueueEta(
            @PathVariable Long id, 
            @RequestParam int targetNumber) {
        if (targetNumber < 0) {
            throw new IllegalArgumentException("Target number tidak boleh negatif");
        }
        com.jkn.backend.dto.EtaResponse etaResponse = queueEtaService.calculateEta(id, targetNumber);
        return ResponseEntity.ok(ApiResponse.success(etaResponse));
    }

    @PutMapping("/{id}/next")
    public ResponseEntity<ApiResponse<QueueResponse>> nextQueue(@PathVariable Long id) {
        QueueResponse response = queueService.nextQueue(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    @ExceptionHandler(com.jkn.backend.exception.QueueInProgressException.class)
    public ResponseEntity<java.util.Map<String, Object>> handleQueueInProgress(com.jkn.backend.exception.QueueInProgressException ex) {
        java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
        errorResponse.put("error_code", "QUEUE_IN_PROGRESS");
        errorResponse.put("retryable", ex.isRetryable());
        errorResponse.put("retry_after", ex.getRetryAfter());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}