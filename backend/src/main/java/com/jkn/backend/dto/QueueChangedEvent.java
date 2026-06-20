package com.jkn.backend.dto;

import java.time.LocalDateTime;

public class QueueChangedEvent {
    private Long queueId;
    private Integer currentNumber;
    private Integer nextNumber;
    private LocalDateTime timestamp;

    public QueueChangedEvent() {
    }

    public QueueChangedEvent(Long queueId, Integer currentNumber, Integer nextNumber, LocalDateTime timestamp) {
        this.queueId = queueId;
        this.currentNumber = currentNumber;
        this.nextNumber = nextNumber;
        this.timestamp = timestamp;
    }

    public Long getQueueId() {
        return queueId;
    }

    public void setQueueId(Long queueId) {
        this.queueId = queueId;
    }

    public Integer getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(Integer currentNumber) {
        this.currentNumber = currentNumber;
    }

    public Integer getNextNumber() {
        return nextNumber;
    }

    public void setNextNumber(Integer nextNumber) {
        this.nextNumber = nextNumber;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
