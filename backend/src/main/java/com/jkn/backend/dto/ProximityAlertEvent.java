package com.jkn.backend.dto;

public class ProximityAlertEvent {
    private Long queueId;
    private Integer currentNumber;

    public ProximityAlertEvent() {}

    public ProximityAlertEvent(Long queueId, Integer currentNumber) {
        this.queueId = queueId;
        this.currentNumber = currentNumber;
    }

    public Long getQueueId() { return queueId; }
    public void setQueueId(Long queueId) { this.queueId = queueId; }

    public Integer getCurrentNumber() { return currentNumber; }
    public void setCurrentNumber(Integer currentNumber) { this.currentNumber = currentNumber; }
}