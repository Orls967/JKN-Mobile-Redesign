package com.jkn.backend.service;

import com.jkn.backend.dto.CreateQueueRequest;
import com.jkn.backend.dto.QueueChangedEvent;
import com.jkn.backend.dto.QueueProximityEvent;
import com.jkn.backend.dto.QueueResponse;
import com.jkn.backend.entity.QueueCallLog;
import com.jkn.backend.entity.QueueCounter;
import com.jkn.backend.exception.ResourceNotFoundException;
import com.jkn.backend.repository.QueueCallLogRepository;
import com.jkn.backend.repository.QueueCounterRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueueServiceImpl implements QueueService {

    private final QueueCounterRepository queueCounterRepository;
    private final QueueCallLogRepository queueCallLogRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public QueueServiceImpl(QueueCounterRepository queueCounterRepository, QueueCallLogRepository queueCallLogRepository, SimpMessagingTemplate messagingTemplate) {
        this.queueCounterRepository = queueCounterRepository;
        this.queueCallLogRepository = queueCallLogRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public QueueResponse createQueue(CreateQueueRequest request) {
        QueueCounter queueCounter = new QueueCounter();
        queueCounter.setCounterName(request.getCounterName());
        queueCounter.setCurrentNumber(0);
        queueCounter.setNextNumber(1);

        QueueCounter saved = queueCounterRepository.save(queueCounter);
        return mapToResponse(saved);
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
    public QueueResponse nextQueue(Long id) {
        QueueCounter queueCounter = queueCounterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + id));

        // Update logic: currentNumber becomes the old nextNumber, nextNumber increments by 1
        queueCounter.setCurrentNumber(queueCounter.getNextNumber());
        queueCounter.setNextNumber(queueCounter.getNextNumber() + 1);

        QueueCounter saved = queueCounterRepository.save(queueCounter);

        // Log the call
        QueueCallLog callLog = new QueueCallLog(saved.getId(), saved.getCurrentNumber());
        queueCallLogRepository.save(callLog);

        // Build Event payload
        QueueChangedEvent event = new QueueChangedEvent(
                saved.getId(),
                saved.getCurrentNumber(),
                saved.getNextNumber(),
                LocalDateTime.now()
        );

        // Broadcast to specific queue topic
        messagingTemplate.convertAndSend("/topic/queue/" + id, event);

        // Generate and broadcast proximity events for the next 3 patients
        int current = saved.getCurrentNumber();
        for (int i = 1; i <= 3; i++) {
            int targetPatientNumber = current + i;
            QueueProximityEvent proxEvent = new QueueProximityEvent(
                    saved.getId(),
                    current,
                    targetPatientNumber,
                    i,
                    LocalDateTime.now()
            );
            messagingTemplate.convertAndSend("/topic/queue/" + id + "/proximity", proxEvent);
        }

        return mapToResponse(saved);
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
}
