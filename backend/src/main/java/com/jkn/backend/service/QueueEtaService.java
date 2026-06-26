package com.jkn.backend.service;

import com.jkn.backend.entity.QueueCallLog;
import com.jkn.backend.entity.QueueCounter;
import com.jkn.backend.exception.ResourceNotFoundException;
import com.jkn.backend.repository.QueueCallLogRepository;
import com.jkn.backend.repository.QueueCounterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class QueueEtaService {

    private static final Logger log = LoggerFactory.getLogger(QueueEtaService.class);

    private final QueueCallLogRepository queueCallLogRepository;
    private final QueueCounterRepository queueCounterRepository;

    public QueueEtaService(QueueCallLogRepository queueCallLogRepository, QueueCounterRepository queueCounterRepository) {
        this.queueCallLogRepository = queueCallLogRepository;
        this.queueCounterRepository = queueCounterRepository;
    }

    public com.jkn.backend.dto.EtaResponse calculateEta(Long queueId, int targetNumber) {
        QueueCounter queueCounter = queueCounterRepository.findById(queueId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + queueId));

        List<QueueCallLog> logs = queueCallLogRepository.findTop10ByQueueCounterIdOrderByCalledAtDesc(queueId);

        long avgServiceSeconds = 180; // Default 3 minutes

        if (logs.size() >= 2) {
            long totalSeconds = 0;
            // logs is descending, so logs.get(0) is the most recent
            for (int i = 0; i < logs.size() - 1; i++) {
                QueueCallLog newer = logs.get(i);
                QueueCallLog older = logs.get(i + 1);
                totalSeconds += Duration.between(older.getCalledAt(), newer.getCalledAt()).getSeconds();
            }
            avgServiceSeconds = totalSeconds / (logs.size() - 1);
            
            // Save the newly calculated average to the DB
            queueCounter.setAverageServiceTime(avgServiceSeconds);
            queueCounterRepository.save(queueCounter);
        } else if (queueCounter.getAverageServiceTime() != null && queueCounter.getAverageServiceTime() > 0) {
            // Use historical average if not enough recent logs
            avgServiceSeconds = queueCounter.getAverageServiceTime();
        } else {
            log.warn("ETA using fallback: queue_id={} log_count={} fallback_seconds=180",
                    queueId, logs.size());
        }

        int currentNumber = queueCounter.getCurrentNumber();
        int remaining = targetNumber - currentNumber;
        int etaMinutes = 0;

        if (remaining > 0) {
            long etaSeconds = remaining * avgServiceSeconds;
            etaMinutes = (int) (etaSeconds / 60);
        }

        log.info("ETA calculated: queue_id={} target_number={} eta_minutes={} avg_service_seconds={}",
                queueId, targetNumber, etaMinutes, avgServiceSeconds);

        return new com.jkn.backend.dto.EtaResponse(queueId, targetNumber, etaMinutes, avgServiceSeconds);
    }

    /**
     * @deprecated Use {@link #calculateEta(Long, int)} instead to avoid N+1 query pattern.
     */
    @Deprecated
    public int calculateEtaMinutes(Long queueId, int targetNumber) {
        return calculateEta(queueId, targetNumber).getEtaMinutes();
    }

    /**
     * @deprecated Use {@link #calculateEta(Long, int)} instead to avoid N+1 query pattern.
     */
    @Deprecated
    public long getAverageServiceSeconds(Long queueId) {
        QueueCounter queueCounter = queueCounterRepository.findById(queueId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + queueId));
        return queueCounter.getAverageServiceTime() != null && queueCounter.getAverageServiceTime() > 0 
                ? queueCounter.getAverageServiceTime() : 180;
    }
}

