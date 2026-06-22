package com.jkn.backend.dto;

import java.time.Instant;

public class QueueChangedEvent {
    private Long queueId;
    private Integer currentNumber;
    private Integer nextNumber;
    private long timestamp;

    public QueueChangedEvent() {
    }

    public QueueChangedEvent(Long queueId, Integer currentNumber, Integer nextNumber) {
        this.queueId = queueId;
        this.currentNumber = currentNumber;
        this.nextNumber = nextNumber;
        this.timestamp = Instant.now().toEpochMilli(); 
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}