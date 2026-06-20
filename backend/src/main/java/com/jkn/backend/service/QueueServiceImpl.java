package com.jkn.backend.service;

import com.jkn.backend.dto.CreateQueueRequest;
import com.jkn.backend.dto.QueueChangedEvent;
import com.jkn.backend.dto.QueueResponse;
import com.jkn.backend.entity.QueueCounter;
import com.jkn.backend.exception.ResourceNotFoundException;
import com.jkn.backend.repository.QueueCounterRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class QueueServiceImpl implements QueueService {

    private final QueueCounterRepository queueCounterRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public QueueServiceImpl(QueueCounterRepository queueCounterRepository, SimpMessagingTemplate messagingTemplate) {
        this.queueCounterRepository = queueCounterRepository;
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
    public QueueResponse nextQueue(Long id) {
        QueueCounter queueCounter = queueCounterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + id));

        // Update logic: currentNumber becomes the old nextNumber, nextNumber increments by 1
        queueCounter.setCurrentNumber(queueCounter.getNextNumber());
        queueCounter.setNextNumber(queueCounter.getNextNumber() + 1);

        QueueCounter saved = queueCounterRepository.save(queueCounter);

        // Build Event payload
        QueueChangedEvent event = new QueueChangedEvent(
                saved.getId(),
                saved.getCurrentNumber(),
                saved.getNextNumber(),
                LocalDateTime.now()
        );

        // Broadcast to specific queue topic
        messagingTemplate.convertAndSend("/topic/queue/" + id, event);

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
