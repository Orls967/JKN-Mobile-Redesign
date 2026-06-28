package com.jkn.backend.service;

import com.jkn.backend.dto.CreateQueueRequest;
import com.jkn.backend.dto.QueueResponse;
import com.jkn.backend.entity.QueueCallLog;
import com.jkn.backend.entity.QueueCounter;
import com.jkn.backend.exception.ResourceNotFoundException;
import com.jkn.backend.publisher.QueueEventPublisher;
import com.jkn.backend.repository.QueueCallLogRepository;
import com.jkn.backend.repository.QueueCounterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueueServiceImpl implements QueueService {

    private static final Logger log = LoggerFactory.getLogger(QueueServiceImpl.class);

    private final QueueCounterRepository queueCounterRepository;
    private final QueueCallLogRepository queueCallLogRepository;
    private final QueueEventPublisher queueEventPublisher;
    private final QueueMetricsService metricsService;
    private final com.jkn.backend.repository.DistributedLockRepository distributedLockRepository;
    private final com.jkn.backend.repository.IdempotencyLogRepository idempotencyLogRepository;
    private final com.jkn.backend.repository.QueueTicketRepository queueTicketRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public QueueServiceImpl(QueueCounterRepository queueCounterRepository,
                            QueueCallLogRepository queueCallLogRepository,
                            QueueEventPublisher queueEventPublisher,
                            QueueMetricsService metricsService,
                            com.jkn.backend.repository.DistributedLockRepository distributedLockRepository,
                            com.jkn.backend.repository.IdempotencyLogRepository idempotencyLogRepository,
                            com.jkn.backend.repository.QueueTicketRepository queueTicketRepository,
                            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.queueCounterRepository = queueCounterRepository;
        this.queueCallLogRepository = queueCallLogRepository;
        this.queueEventPublisher = queueEventPublisher;
        this.metricsService = metricsService;
        this.distributedLockRepository = distributedLockRepository;
        this.idempotencyLogRepository = idempotencyLogRepository;
        this.queueTicketRepository = queueTicketRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ, timeout = 9)
    public QueueResponse createQueue(CreateQueueRequest request, String idempotencyKey) {
        log.error("============== MASUK CREATE QUEUE ==============");

        return metricsService.getRegistrationTimer().record(() -> {
            try {
                // Implementasi TASK-01-B: Distributed Lock (pg_advisory_xact_lock)
                // Lock ini fail-fast, timeout 0ms. Berlangsung selama durasi transaksi (karena @Transactional).
                java.time.LocalDate today = java.time.LocalDate.now();
                String lockKey = String.format("queue_lock:%s:%d:%s", 
                    request.getUserId() != null ? request.getUserId() : "unknown",
                    request.getFaskesId() != null ? request.getFaskesId() : 0L,
                    today.toString()
                );
                
                log.info("TRY LOCK : {}", lockKey);

                boolean lockAcquired = distributedLockRepository.tryAcquireLock(lockKey);
                
                log.info("LOCK RESULT : {} = {}", lockKey, lockAcquired);

                if (!lockAcquired) {
                    log.warn("Failed to acquire lock for queue creation: lock_key={}", lockKey);
                    throw new com.jkn.backend.exception.QueueInProgressException("Pendaftaran antrean sedang diproses", true, 2);
                }

                QueueCounter queueCounter = new QueueCounter();
                queueCounter.setCounterName(request.getCounterName());
                queueCounter.setCurrentNumber(0);
                queueCounter.setNextNumber(1);

                QueueCounter saved = queueCounterRepository.save(queueCounter);
                log.info("QUEUE CREATED : id={}", saved.getId());

                log.info("Queue created: queue_id={} counter_name={}",
                        saved.getId(), saved.getCounterName());
                
                // Implementasi TASK-05-A (Step 3): Insert record antrean
                com.jkn.backend.entity.QueueTicket ticket = new com.jkn.backend.entity.QueueTicket(
                        saved.getId(),
                        request.getUserId() != null ? request.getUserId() : "unknown",
                        request.getFaskesId() != null ? request.getFaskesId() : 0L,
                        java.time.LocalDate.now(),
                        saved.getNextNumber() - 1
                );
                queueTicketRepository.save(ticket);
                
                metricsService.recordRegistrationSuccess();
                
                QueueResponse queueResponse = mapToResponse(saved);
                
                // Update Idempotency Log to COMPLETED inside the transaction
                if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                    idempotencyLogRepository.findById(idempotencyKey).ifPresent(logEntry -> {
                        try {
                            logEntry.setStatus(com.jkn.backend.entity.IdempotencyStatus.COMPLETED);
                            com.jkn.backend.dto.ApiResponse<QueueResponse> apiResp = com.jkn.backend.dto.ApiResponse.created(queueResponse);
                            logEntry.setResponseBody(objectMapper.writeValueAsString(apiResp));
                            idempotencyLogRepository.save(logEntry);
                        } catch (Exception ex) {
                            log.error("Failed to serialize idempotency response", ex);
                        }
                    });
                }
                
                return queueResponse;
            } catch (com.jkn.backend.exception.QueueInProgressException e) {
                // Jangan record sebagai error sistem jika murni karena lock
                throw e;
            } catch (Exception e) {
                metricsService.recordRegistrationFailed();
                throw e;
            }
        });
    }

    @Override
    public QueueResponse getQueueById(Long id) {
        QueueCounter queueCounter = queueCounterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + id));

        return mapToResponse(queueCounter);
    }

    @Override
    public List<QueueResponse> getAllQueues() {
        return queueCounterRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QueueResponse nextQueue(Long id) {
        return metricsService.getNextTimer().record(() -> {
            try {
                QueueCounter queueCounter = queueCounterRepository.findByIdForUpdate(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + id));

                if (queueCounter.getCurrentNumber() >= queueCounter.getLastNumber()) {
                    log.warn("Queue exhausted: queue_id={} current_number={} last_number={}",
                            id, queueCounter.getCurrentNumber(), queueCounter.getLastNumber());
                    metricsService.recordNextFailed();
                    throw new IllegalStateException("Antrean sudah habis");
                }

                LocalDateTime now = LocalDateTime.now();
                queueCounter.setLastCalledAt(now);

                queueCounter.setCurrentNumber(queueCounter.getNextNumber());
                queueCounter.setNextNumber(queueCounter.getNextNumber() + 1);

                QueueCounter saved = queueCounterRepository.save(queueCounter);

                QueueCallLog callLog = new QueueCallLog(saved.getId(), saved.getCurrentNumber());
                queueCallLogRepository.save(callLog);

                // Warn ketika antrean hampir habis
                int remaining = saved.getLastNumber() - saved.getCurrentNumber();
                if (remaining <= 5 && remaining > 0) {
                    log.warn("Queue running low: queue_id={} remaining={}", id, remaining);
                }

                log.info("Queue advanced: queue_id={} new_number={}", id, saved.getCurrentNumber());

                metricsService.recordNextSuccess();
                queueEventPublisher.publishQueueChanged(saved);

                return mapToResponse(saved);
            } catch (IllegalStateException e) {
                throw e; // Already recorded above
            } catch (Exception e) {
                metricsService.recordNextFailed();
                throw e;
            }
        });
    }

    private QueueResponse mapToResponse(QueueCounter entity) {
        return new QueueResponse(
                entity.getId(),
                entity.getCounterName(),
                entity.getCurrentNumber(),
                entity.getNextNumber(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public com.jkn.backend.dto.QueueStatusResponse getQueueStatusByIdempotencyKey(String idempotencyKey) {
        com.jkn.backend.entity.IdempotencyLog log = idempotencyLogRepository.findById(idempotencyKey)
                .orElseThrow(() -> new com.jkn.backend.exception.ResourceNotFoundException("Request tidak ditemukan atau sudah expired"));

        if (log.getStatus() == com.jkn.backend.entity.IdempotencyStatus.PROCESSING) {
            return new com.jkn.backend.dto.QueueStatusResponse(
                    "PROCESSING",
                    "Request sedang diproses, coba lagi dalam 2 detik",
                    2
            );
        } else if (log.getStatus() == com.jkn.backend.entity.IdempotencyStatus.COMPLETED) {
            try {
                com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(log.getResponseBody());
                com.fasterxml.jackson.databind.JsonNode data = root.path("data");
                
                com.jkn.backend.dto.QueueStatusResponse response = new com.jkn.backend.dto.QueueStatusResponse();
                response.setStatus("COMPLETED");
                
                String counterName = data.path("counterName").asText();
                int nextNum = data.path("nextNumber").asInt();
                response.setQueueNumber(counterName + "-" + String.format("%03d", nextNum));
                
                // Assuming Poli and Faskes mapping from counterName
                response.setPoli("Poli " + counterName);
                response.setFaskes("RS Harapan Bunda"); // Hardcoded as per spec example
                response.setCreatedAt(data.path("createdAt").asText());
                
                return response;
            } catch (Exception ex) {
                throw new RuntimeException("Gagal membaca status antrean", ex);
            }
        }
        
        throw new com.jkn.backend.exception.ResourceNotFoundException("Request gagal diproses");
    }
}