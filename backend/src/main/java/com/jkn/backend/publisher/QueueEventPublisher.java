package com.jkn.backend.publisher;

import com.jkn.backend.dto.QueueChangedEvent;
import com.jkn.backend.dto.ProximityAlertEvent; // Memanggil DTO yang baru
import com.jkn.backend.entity.QueueCounter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class QueueEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public QueueEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishQueueChanged(QueueCounter queueCounter) {
        // Broadcast utama tetap sama
        QueueChangedEvent event = new QueueChangedEvent(
                queueCounter.getId(),
                queueCounter.getCurrentNumber(),
                queueCounter.getNextNumber()
        );

        messagingTemplate.convertAndSend("/topic/queue/" + queueCounter.getId(), event);

        // SESUAI JIRA: Broadcast proximity dikirim sebagai event tambahan di sini
        ProximityAlertEvent proxEvent = new ProximityAlertEvent(
                queueCounter.getId(),
                queueCounter.getCurrentNumber()
        );
        messagingTemplate.convertAndSend("/topic/queue/" + queueCounter.getId() + "/proximity", proxEvent);
    }
}