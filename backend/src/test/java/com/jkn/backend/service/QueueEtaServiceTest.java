package com.jkn.backend.service;

import com.jkn.backend.dto.EtaResponse;
import com.jkn.backend.entity.QueueCallLog;
import com.jkn.backend.entity.QueueCounter;
import com.jkn.backend.repository.QueueCallLogRepository;
import com.jkn.backend.repository.QueueCounterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class QueueEtaServiceTest {

    private QueueCallLogRepository queueCallLogRepository;
    private QueueCounterRepository queueCounterRepository;
    private QueueEtaService queueEtaService;

    @BeforeEach
    void setUp() {
        queueCallLogRepository = Mockito.mock(QueueCallLogRepository.class);
        queueCounterRepository = Mockito.mock(QueueCounterRepository.class);
        queueEtaService = new QueueEtaService(queueCallLogRepository, queueCounterRepository);
    }

    @Test
    void calculateEta_shouldReturnCorrectEta_whenLogsExist() {
        // Arrange (TC-1.4: Happy Path, calculateEtaMinutes)
        Long queueId = 1L;
        QueueCounter queueCounter = new QueueCounter();
        queueCounter.setId(queueId);
        queueCounter.setCurrentNumber(5); // Sisa 5 antrean ke target 10

        when(queueCounterRepository.findById(queueId)).thenReturn(Optional.of(queueCounter));

        // Buat log dengan selisih waktu 2 menit (120 detik)
        QueueCallLog newer = new QueueCallLog(queueId, 5);
        newer.setCalledAt(LocalDateTime.of(2026, 6, 28, 10, 2, 0));
        
        QueueCallLog older = new QueueCallLog(queueId, 4);
        older.setCalledAt(LocalDateTime.of(2026, 6, 28, 10, 0, 0));

        when(queueCallLogRepository.findTop10ByQueueCounterIdOrderByCalledAtDesc(queueId))
                .thenReturn(Arrays.asList(newer, older));

        // Act
        EtaResponse response = queueEtaService.calculateEta(queueId, 10); // Target = 10

        // Assert
        assertEquals(10, response.getEtaMinutes()); // Sisa 5 * 2 menit = 10 menit
        assertEquals(120, response.getAvgServiceSeconds()); // 120 detik
    }

    @Test
    void calculateEta_shouldReturnZero_whenTargetNumberIsBeforeCurrent() {
        // Arrange (TC-1.5: targetNumber <= currentNumber)
        Long queueId = 1L;
        QueueCounter queueCounter = new QueueCounter();
        queueCounter.setId(queueId);
        queueCounter.setCurrentNumber(10); // Saat ini sudah nomor 10

        when(queueCounterRepository.findById(queueId)).thenReturn(Optional.of(queueCounter));
        when(queueCallLogRepository.findTop10ByQueueCounterIdOrderByCalledAtDesc(queueId))
                .thenReturn(Collections.emptyList());

        // Act
        EtaResponse response = queueEtaService.calculateEta(queueId, 5); // Target = 5 (Sudah lewat)

        // Assert
        assertEquals(0, response.getEtaMinutes());
    }

    @Test
    void calculateEta_shouldReturnDefaultEta_whenNoLogsExist() {
        // Arrange (TC-1.6: new queue, no history)
        Long queueId = 1L;
        QueueCounter queueCounter = new QueueCounter();
        queueCounter.setId(queueId);
        queueCounter.setCurrentNumber(0); 

        when(queueCounterRepository.findById(queueId)).thenReturn(Optional.of(queueCounter));
        when(queueCallLogRepository.findTop10ByQueueCounterIdOrderByCalledAtDesc(queueId))
                .thenReturn(Collections.emptyList());

        // Act
        EtaResponse response = queueEtaService.calculateEta(queueId, 5); // Target = 5

        // Assert
        assertEquals(15, response.getEtaMinutes()); // Sisa 5 * default 3 menit (180s) = 15 menit
        assertEquals(180, response.getAvgServiceSeconds()); 
    }
    @Test
    void calculateEta_shouldThrowException_whenQueueNotFound() {
        Long queueId = 999L;
        when(queueCounterRepository.findById(queueId)).thenReturn(Optional.empty());
        
        org.junit.jupiter.api.Assertions.assertThrows(
            com.jkn.backend.exception.ResourceNotFoundException.class,
            () -> queueEtaService.calculateEta(queueId, 5)
        );
    }
    
    @Test
    void deprecatedMethods_shouldReturnCorrectly() {
        Long queueId = 1L;
        QueueCounter queueCounter = new QueueCounter();
        queueCounter.setId(queueId);
        queueCounter.setCurrentNumber(0);
        queueCounter.setAverageServiceTime(120L); // Not null
        
        when(queueCounterRepository.findById(queueId)).thenReturn(Optional.of(queueCounter));
        when(queueCallLogRepository.findTop10ByQueueCounterIdOrderByCalledAtDesc(queueId))
                .thenReturn(Collections.emptyList());
                
        int etaMinutes = queueEtaService.calculateEtaMinutes(queueId, 5);
        assertEquals(10, etaMinutes);
        
        long avgSeconds = queueEtaService.getAverageServiceSeconds(queueId);
        assertEquals(120L, avgSeconds);
    }
}
