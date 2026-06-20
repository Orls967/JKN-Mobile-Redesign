package com.jkn.backend.dto;

import java.time.LocalDateTime;

public class QueueProximityEvent {
    private Long queueId;
    private Integer currentNumber;
    private Integer patientNumber;
    private Integer remainingQueue;
    private LocalDateTime timestamp;

    public QueueProximityEvent() {}

    public QueueProximityEvent(Long queueId, Integer currentNumber, Integer patientNumber, Integer remainingQueue, LocalDateTime timestamp) {
        this.queueId = queueId;
        this.currentNumber = currentNumber;
        this.patientNumber = patientNumber;
        this.remainingQueue = remainingQueue;
        this.timestamp = timestamp;
    }

    public Long getQueueId() { return queueId; }
    public void setQueueId(Long queueId) { this.queueId = queueId; }

    public Integer getCurrentNumber() { return currentNumber; }
    public void setCurrentNumber(Integer currentNumber) { this.currentNumber = currentNumber; }

    public Integer getPatientNumber() { return patientNumber; }
    public void setPatientNumber(Integer patientNumber) { this.patientNumber = patientNumber; }

    public Integer getRemainingQueue() { return remainingQueue; }
    public void setRemainingQueue(Integer remainingQueue) { this.remainingQueue = remainingQueue; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
