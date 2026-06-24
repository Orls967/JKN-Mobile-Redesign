package com.jkn.backend.publisher;

import com.jkn.backend.dto.QueueChangedEvent;
import com.jkn.backend.dto.ProximityAlertEvent; // Pastikan import DTO baru ini ada
import com.jkn.backend.entity.QueueCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class QueueEventPublisherTest {

    private SimpMessagingTemplate messagingTemplate;
    private QueueEventPublisher queueEventPublisher;

    @BeforeEach
    void setUp() {
        messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
        queueEventPublisher = new QueueEventPublisher(messagingTemplate);
    }

    @Test
    void publishQueueChanged_shouldSendCorrectPayloadToCorrectTopic() {
        // Arrange
        QueueCounter queue = new QueueCounter();
        queue.setId(1L);
        queue.setCounterName("Poli Umum");
        queue.setCurrentNumber(5);
        queue.setNextNumber(6);

        // Act
        queueEventPublisher.publishQueueChanged(queue);

        // Assert
        ArgumentCaptor<QueueChangedEvent> captor = ArgumentCaptor.forClass(QueueChangedEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/queue/1"), captor.capture());

        QueueChangedEvent capturedEvent = captor.getValue();
        assertEquals(1L, capturedEvent.getQueueId());
        assertEquals(5, capturedEvent.getCurrentNumber());
        assertEquals(6, capturedEvent.getNextNumber());
        assertNotNull(capturedEvent.getTimestamp());
    }

    // TAMBAHAN TEST: Menguji apakah ProximityAlertEvent terkirim dengan benar (Sesuai Jira)
    @Test
    void publishQueueProximity_shouldSendProximityAlertEvent() {
        // Arrange
        QueueCounter queue = new QueueCounter();
        queue.setId(2L);
        queue.setCurrentNumber(10);

        // Act
        queueEventPublisher.publishQueueProximity(queue);

        // Assert
        ArgumentCaptor<ProximityAlertEvent> captor = ArgumentCaptor.forClass(ProximityAlertEvent.class);
        // Memastikan topic yang ditembak benar ada tambahan "/proximity"
        verify(messagingTemplate).convertAndSend(eq("/topic/queue/2/proximity"), captor.capture());

        // Memastikan isi payload sangat ringan (hanya id dan angka saat ini)
        ProximityAlertEvent capturedEvent = captor.getValue();
        assertEquals(2L, capturedEvent.getQueueId());
        assertEquals(10, capturedEvent.getCurrentNumber());
    }
}