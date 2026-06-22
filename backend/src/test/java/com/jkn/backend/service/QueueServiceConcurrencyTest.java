package com.jkn.backend.service;

import com.jkn.backend.dto.QueueResponse;
import com.jkn.backend.entity.QueueCounter;
import com.jkn.backend.repository.QueueCallLogRepository;
import com.jkn.backend.repository.QueueCounterRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class QueueServiceConcurrencyTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueCounterRepository queueCounterRepository;

    @Autowired
    private QueueCallLogRepository queueCallLogRepository;

    private Long testQueueId;

    @BeforeEach
    void setUp() {
        // Clear old data
        queueCallLogRepository.deleteAll();
        queueCounterRepository.deleteAll();

        // Create a test queue
        QueueCounter queue = new QueueCounter();
        queue.setCounterName("Poli Concurrency");
        queue.setCurrentNumber(0);
        queue.setNextNumber(1);
        QueueCounter saved = queueCounterRepository.save(queue);
        testQueueId = saved.getId();
    }

    @AfterEach
    void tearDown() {
        queueCallLogRepository.deleteAll();
        queueCounterRepository.deleteAll();
    }

    @Test
    void testNextQueue_Concurrency_TwoParallelRequests() throws InterruptedException {
        // Arrange
        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // Act
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    queueService.nextQueue(testQueueId);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to finish
        latch.await();

        // Assert
        QueueCounter updatedQueue = queueCounterRepository.findById(testQueueId).orElseThrow();
        long callLogCount = queueCallLogRepository.count();

        // Since currentNumber starts at 0, two next() calls should make it 2.
        assertEquals(2, updatedQueue.getCurrentNumber(), "Current number should be exactly 2");
        assertEquals(3, updatedQueue.getNextNumber(), "Next number should be exactly 3");
        
        // Ensure exactly 2 logs were created
        assertEquals(2, callLogCount, "There should be exactly 2 call logs created");
    }
}
