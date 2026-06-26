package com.jkn.backend.publisher;

import com.jkn.backend.dto.QueueChangedEvent;
import com.jkn.backend.dto.ProximityAlertEvent; // Memanggil DTO yang baru
import com.jkn.backend.entity.QueueCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class QueueEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(QueueEventPublisher.class);

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
        log.info("WebSocket event published: topic=/topic/queue/{} current_number={}",
                queueCounter.getId(), queueCounter.getCurrentNumber());

        // SESUAI JIRA: Broadcast proximity dikirim sebagai event tambahan di sini
        ProximityAlertEvent proxEvent = new ProximityAlertEvent(
                queueCounter.getId(),
                queueCounter.getCurrentNumber()
        );
        messagingTemplate.convertAndSend("/topic/queue/" + queueCounter.getId() + "/proximity", proxEvent);
        log.info("WebSocket proximity event published: topic=/topic/queue/{}/proximity current_number={}",
                queueCounter.getId(), queueCounter.getCurrentNumber());
    }
}