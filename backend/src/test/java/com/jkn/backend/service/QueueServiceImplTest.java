package com.jkn.backend.service;

import com.jkn.backend.dto.CreateQueueRequest;
import com.jkn.backend.dto.QueueResponse;
import com.jkn.backend.entity.QueueCallLog;
import com.jkn.backend.entity.QueueCounter;
import com.jkn.backend.publisher.QueueEventPublisher;
import com.jkn.backend.repository.QueueCallLogRepository;
import com.jkn.backend.repository.QueueCounterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QueueServiceImplTest {

    private QueueCounterRepository queueCounterRepository;
    private QueueCallLogRepository queueCallLogRepository;
    private QueueEventPublisher queueEventPublisher;
    private com.jkn.backend.repository.DistributedLockRepository distributedLockRepository;
    private QueueMetricsService metricsService;
    private QueueServiceImpl queueService;

    @BeforeEach
    void setUp() {
        queueCounterRepository = Mockito.mock(QueueCounterRepository.class);
        queueCallLogRepository = Mockito.mock(QueueCallLogRepository.class);
        queueEventPublisher = Mockito.mock(QueueEventPublisher.class);
        
        io.micrometer.core.instrument.simple.SimpleMeterRegistry registry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        QueueMetricsService metricsService = new QueueMetricsService(registry);

        distributedLockRepository = Mockito.mock(com.jkn.backend.repository.DistributedLockRepository.class);
        
        queueService = new QueueServiceImpl(
            queueCounterRepository, 
            queueCallLogRepository, 
            queueEventPublisher, 
            metricsService,
            distributedLockRepository
        );
    }

    @Test
    void createQueue_shouldAcquireLockAndReturnResponse() {
        // Arrange
        CreateQueueRequest request = new CreateQueueRequest("Poli Gigi", "user123", 1L);
        QueueCounter savedQueue = new QueueCounter();
        savedQueue.setId(1L);
        savedQueue.setCounterName("Poli Gigi");
        savedQueue.setCurrentNumber(0);
        savedQueue.setNextNumber(1);

        Mockito.when(distributedLockRepository.tryAcquireLock(Mockito.anyString())).thenReturn(true);
        Mockito.when(queueCounterRepository.save(Mockito.any(QueueCounter.class))).thenReturn(savedQueue);

        // Act
        QueueResponse response = queueService.createQueue(request);

        // Assert
        org.junit.jupiter.api.Assertions.assertNotNull(response);
        org.junit.jupiter.api.Assertions.assertEquals("Poli Gigi", response.getCounterName());
    }

    @Test
    void nextQueue_shouldIncrementNumbersAndLogAndPublish() {
        // Arrange
        Long queueId = 1L;
        QueueCounter existingQueue = new QueueCounter();
        existingQueue.setId(queueId);
        existingQueue.setCurrentNumber(5);
        existingQueue.setNextNumber(6);
        existingQueue.setLastNumber(50);

        when(queueCounterRepository.findByIdForUpdate(queueId)).thenReturn(Optional.of(existingQueue));
        when(queueCounterRepository.save(any(QueueCounter.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        QueueResponse response = queueService.nextQueue(queueId);

        // Assert
        // 1. Current and Next should increment correctly
        assertEquals(6, response.getCurrentNumber());
        assertEquals(7, response.getNextNumber());

        // 2. QueueCallLog should be saved
        ArgumentCaptor<QueueCallLog> logCaptor = ArgumentCaptor.forClass(QueueCallLog.class);
        verify(queueCallLogRepository).save(logCaptor.capture());
        assertEquals(queueId, logCaptor.getValue().getQueueCounterId());
        assertEquals(6, logCaptor.getValue().getTicketNumber());
        verify(queueCounterRepository).save(any(QueueCounter.class));
        verify(queueCallLogRepository).save(any(QueueCallLog.class));
        verify(queueEventPublisher).publishQueueChanged(any(QueueCounter.class));
    }

    @Test
    void nextQueue_shouldThrowException_whenQueueIsExhausted() {
        // Arrange
        Long queueId = 1L;
        QueueCounter existingQueue = new QueueCounter();
        existingQueue.setId(queueId);
        existingQueue.setCurrentNumber(50);
        existingQueue.setNextNumber(51);
        existingQueue.setLastNumber(50);

        when(queueCounterRepository.findByIdForUpdate(queueId)).thenReturn(Optional.of(existingQueue));

        // Act & Assert
        IllegalStateException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> queueService.nextQueue(queueId)
        );
        assertEquals("Antrean sudah habis", exception.getMessage());
    }
}
