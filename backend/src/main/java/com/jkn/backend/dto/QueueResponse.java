package com.jkn.backend.dto;

import java.time.LocalDateTime;

public class QueueResponse {

    private Long id;
    private String counterName;
    private Integer currentNumber;
    private Integer nextNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public QueueResponse() {
    }

    public QueueResponse(Long id, String counterName, Integer currentNumber, Integer nextNumber, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.counterName = counterName;
        this.currentNumber = currentNumber;
        this.nextNumber = nextNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCounterName() { return counterName; }
    public void setCounterName(String counterName) { this.counterName = counterName; }

    public Integer getCurrentNumber() { return currentNumber; }
    public void setCurrentNumber(Integer currentNumber) { this.currentNumber = currentNumber; }

    public Integer getNextNumber() { return nextNumber; }
    public void setNextNumber(Integer nextNumber) { this.nextNumber = nextNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
