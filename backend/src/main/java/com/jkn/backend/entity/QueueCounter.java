package com.jkn.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "queue_counter")
public class QueueCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "counter_name", nullable = false, length = 100)
    private String counterName;

    @Column(name = "current_number", nullable = false)
    private Integer currentNumber = 0;

    @Column(name = "next_number", nullable = false)
    private Integer nextNumber = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_number", nullable = false, columnDefinition = "integer default 50")
    private Integer lastNumber = 50;

    @Column(name = "last_called_at")
    private LocalDateTime lastCalledAt;

    @Column(name = "average_service_time")
    private Long averageServiceTime = 0L; 

    public QueueCounter() {
    }

    public QueueCounter(Long id, String counterName, Integer currentNumber, Integer nextNumber, LocalDateTime createdAt, LocalDateTime updatedAt) {
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

    public Integer getLastNumber() { return lastNumber; }
    public void setLastNumber(Integer lastNumber) { this.lastNumber = lastNumber; }

    // --- GETTER & SETTER UNTUK ETA ---
    public LocalDateTime getLastCalledAt() { return lastCalledAt; }
    public void setLastCalledAt(LocalDateTime lastCalledAt) { this.lastCalledAt = lastCalledAt; }

    public Long getAverageServiceTime() { return averageServiceTime; }
    public void setAverageServiceTime(Long averageServiceTime) { this.averageServiceTime = averageServiceTime; }
}