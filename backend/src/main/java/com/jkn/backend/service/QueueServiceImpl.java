package com.jkn.backend.service;

import com.jkn.backend.dto.CreateQueueRequest;
import com.jkn.backend.dto.QueueResponse;
import com.jkn.backend.entity.QueueCallLog;
import com.jkn.backend.entity.QueueCounter;
import com.jkn.backend.exception.ResourceNotFoundException;
import com.jkn.backend.publisher.QueueEventPublisher;
import com.jkn.backend.repository.QueueCallLogRepository;
import com.jkn.backend.repository.QueueCounterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueueServiceImpl implements QueueService {

    private final QueueCounterRepository queueCounterRepository;
    private final QueueCallLogRepository queueCallLogRepository;
    private final QueueEventPublisher queueEventPublisher;

    public QueueServiceImpl(QueueCounterRepository queueCounterRepository, QueueCallLogRepository queueCallLogRepository, QueueEventPublisher queueEventPublisher) {
        this.queueCounterRepository = queueCounterRepository;
        this.queueCallLogRepository = queueCallLogRepository;
        this.queueEventPublisher = queueEventPublisher;
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
    @Transactional
    public QueueResponse nextQueue(Long id) {
        QueueCounter queueCounter = queueCounterRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + id));

        if (queueCounter.getCurrentNumber() >= queueCounter.getLastNumber()) {
            throw new IllegalStateException("Antrean sudah habis");
        }

        // Update logic: currentNumber becomes the old nextNumber, nextNumber increments by 1
        queueCounter.setCurrentNumber(queueCounter.getNextNumber());
        queueCounter.setNextNumber(queueCounter.getNextNumber() + 1);

        QueueCounter saved = queueCounterRepository.save(queueCounter);

        // Log the call
        QueueCallLog callLog = new QueueCallLog(saved.getId(), saved.getCurrentNumber());
        queueCallLogRepository.save(callLog);

        // Broadcast using the dedicated publisher
        queueEventPublisher.publishQueueChanged(saved);

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
