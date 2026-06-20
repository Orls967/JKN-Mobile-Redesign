package com.jkn.backend.service;

import com.jkn.backend.dto.CreateQueueRequest;
import com.jkn.backend.dto.QueueResponse;
import com.jkn.backend.entity.QueueCounter;
import com.jkn.backend.exception.ResourceNotFoundException;
import com.jkn.backend.repository.QueueCounterRepository;
import org.springframework.stereotype.Service;

@Service
public class QueueServiceImpl implements QueueService {

    private final QueueCounterRepository queueCounterRepository;

    public QueueServiceImpl(QueueCounterRepository queueCounterRepository) {
        this.queueCounterRepository = queueCounterRepository;
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
