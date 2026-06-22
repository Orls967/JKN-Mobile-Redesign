package com.jkn.backend.publisher;

import com.jkn.backend.dto.QueueChangedEvent;
import com.jkn.backend.dto.QueueProximityEvent;
import com.jkn.backend.entity.QueueCounter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class QueueEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public QueueEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishQueueChanged(QueueCounter queueCounter) {
        // We use the existing JSON payload structure (queueId, currentNumber, nextNumber)
        // to maintain compatibility with the current Android frontend.
        QueueChangedEvent event = new QueueChangedEvent(
                queueCounter.getId(),
                queueCounter.getCurrentNumber(),
                queueCounter.getNextNumber()
        );

        messagingTemplate.convertAndSend("/topic/queue/" + queueCounter.getId(), event);
    }

    public void publishQueueProximity(QueueCounter queueCounter) {
        int current = queueCounter.getCurrentNumber();
        for (int i = 1; i <= 3; i++) {
            int targetPatientNumber = current + i;
            QueueProximityEvent proxEvent = new QueueProximityEvent(
                    queueCounter.getId(),
                    current,
                    targetPatientNumber,
                    i,
                    LocalDateTime.now()
            );
            messagingTemplate.convertAndSend("/topic/queue/" + queueCounter.getId() + "/proximity", proxEvent);
        }
    }
}
