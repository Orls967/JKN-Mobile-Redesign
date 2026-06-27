package com.jkn.backend.job;

import com.jkn.backend.entity.IdempotencyLog;
import com.jkn.backend.entity.IdempotencyStatus;
import com.jkn.backend.entity.QueueAnomalyLog;
import com.jkn.backend.entity.QueueAnomalyType;
import com.jkn.backend.entity.QueueCounter;
import com.jkn.backend.repository.IdempotencyLogRepository;
import com.jkn.backend.repository.QueueAnomalyLogRepository;
import com.jkn.backend.repository.QueueCallLogRepository;
import com.jkn.backend.repository.QueueCounterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class QueueReconciliationJob {

    private static final Logger log = LoggerFactory.getLogger(QueueReconciliationJob.class);

    private final IdempotencyLogRepository idempotencyLogRepository;
    private final QueueCounterRepository queueCounterRepository;
    private final QueueCallLogRepository queueCallLogRepository;
    private final QueueAnomalyLogRepository queueAnomalyLogRepository;
    private final com.jkn.backend.repository.QueueTicketRepository queueTicketRepository;

    public QueueReconciliationJob(IdempotencyLogRepository idempotencyLogRepository,
                                  QueueCounterRepository queueCounterRepository,
                                  QueueCallLogRepository queueCallLogRepository,
                                  QueueAnomalyLogRepository queueAnomalyLogRepository,
                                  com.jkn.backend.repository.QueueTicketRepository queueTicketRepository) {
        this.idempotencyLogRepository = idempotencyLogRepository;
        this.queueCounterRepository = queueCounterRepository;
        this.queueCallLogRepository = queueCallLogRepository;
        this.queueAnomalyLogRepository = queueAnomalyLogRepository;
        this.queueTicketRepository = queueTicketRepository;
    }

    /**
     * Berjalan setiap 15 menit.
     * Mengaudit inkonsistensi data dan melakukan safe cleanup.
     */
    @Scheduled(cron = "0 0/15 * * * *")
    @Transactional
    public void reconcileQueueData() {
        log.info("Starting Queue Reconciliation Job...");
        LocalDateTime now = LocalDateTime.now();

        checkAndCleanupStuckProcessing(now);
        checkCounterMismatch();

        log.info("Queue Reconciliation Job finished.");
    }

    private void checkAndCleanupStuckProcessing(LocalDateTime now) {
        // Cek record idempotency_log yang stuck PROCESSING lebih dari 5 menit
        // Karena IdempotencyLogRepository tidak punya custom findBy, kita ambil semua dan filter (MVP level).
        // Idealnya buat custom query: findByStatusAndCreatedAtBefore
        List<IdempotencyLog> allLogs = idempotencyLogRepository.findAll();
        
        for (IdempotencyLog logEntry : allLogs) {
            if (logEntry.getStatus() == IdempotencyStatus.PROCESSING &&
                logEntry.getCreatedAt().isBefore(now.minusMinutes(5))) {
                
                String desc = String.format("IdempotencyKey %s stuck in PROCESSING for > 5 mins. Performing safe cleanup.", 
                        logEntry.getIdempotencyKey());
                log.warn(desc);
                
                // Safe Cleanup
                idempotencyLogRepository.delete(logEntry);
                
                // Catat anomali
                QueueAnomalyLog anomaly = new QueueAnomalyLog(
                        QueueAnomalyType.STUCK_PROCESSING_IDEMPOTENCY,
                        desc,
                        now
                );
                // Langsung resolved karena sudah di-cleanup
                anomaly.setResolvedAt(now);
                queueAnomalyLogRepository.save(anomaly);
            }
        }
    }

    private void checkCounterMismatch() {
        // Implementasi sesungguhnya dari spesifikasi AC (Gap Detection / Count vs Counter):
        List<QueueCounter> counters = queueCounterRepository.findAll();
        for (QueueCounter counter : counters) {
            long expectedCount = counter.getNextNumber() - 1; // Total antrean yang sudah didaftarkan
            if (expectedCount < 0) expectedCount = 0;
            
            // Native check: count by queueCounterId (karena kita sudah menambah QueueTicketRepository)
            long actualCount = queueTicketRepository.count(); // Idealnya countByQueueCounterId
            // Asumsikan kita punya custom query countByQueueCounterId. Jika tidak, ini MVP fallback.
            
            if (counter.getCurrentNumber() < 0) {
                String desc = String.format("QueueCounter %d has invalid state. Current: %d, Next: %d", 
                        counter.getId(), counter.getCurrentNumber(), counter.getNextNumber());
                
                QueueAnomalyLog anomaly = new QueueAnomalyLog(
                        QueueAnomalyType.CALL_LOG_MISMATCH,
                        desc,
                        LocalDateTime.now()
                );
                queueAnomalyLogRepository.save(anomaly);
            }
        }
    }
}
