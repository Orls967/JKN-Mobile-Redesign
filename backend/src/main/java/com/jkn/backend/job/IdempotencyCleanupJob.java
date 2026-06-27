package com.jkn.backend.job;

import com.jkn.backend.repository.IdempotencyLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class IdempotencyCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyCleanupJob.class);
    
    private final IdempotencyLogRepository idempotencyLogRepository;

    public IdempotencyCleanupJob(IdempotencyLogRepository idempotencyLogRepository) {
        this.idempotencyLogRepository = idempotencyLogRepository;
    }

    /**
     * Berjalan setiap 1 jam (Sesuai AC: Cron job berjalan setiap 1 jam).
     * Membersihkan log idempotency yang sudah kadaluarsa (melewati expiresAt).
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredLogs() {
        log.info("Starting Idempotency Cleanup Job...");
        
        int batchSize = 1000;
        int totalDeleted = 0;
        
        try {
            while (true) {
                int deleted = idempotencyLogRepository.deleteExpiredLogsBatch(LocalDateTime.now(), batchSize);
                totalDeleted += deleted;
                
                if (deleted == 0) {
                    break;
                }
                
                // Throttle untuk menghindari DB overload
                Thread.sleep(100);
            }
            log.info("Idempotency Cleanup Job finished successfully. Total deleted: {}", totalDeleted);
        } catch (Exception e) {
            log.error("Idempotency Cleanup Job failed!", e);
        }
    }
}
