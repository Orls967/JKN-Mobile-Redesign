package com.jkn.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "queue_call_log", indexes = {
    @Index(name = "idx_call_log_counter_called_desc", columnList = "queue_counter_id, called_at DESC")
})
public class QueueCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "queue_counter_id", nullable = false)
    private Long queueCounterId;

    @Column(name = "ticket_number", nullable = false)
    private Integer ticketNumber;

    @CreationTimestamp
    @Column(name = "called_at", nullable = false, updatable = false)
    private LocalDateTime calledAt;

    public QueueCallLog() {
    }

    public QueueCallLog(Long queueCounterId, Integer ticketNumber) {
        this.queueCounterId = queueCounterId;
        this.ticketNumber = ticketNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQueueCounterId() { return queueCounterId; }
    public void setQueueCounterId(Long queueCounterId) { this.queueCounterId = queueCounterId; }

    public Integer getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(Integer ticketNumber) { this.ticketNumber = ticketNumber; }

    public LocalDateTime getCalledAt() { return calledAt; }
    public void setCalledAt(LocalDateTime calledAt) { this.calledAt = calledAt; }
}
