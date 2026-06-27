package com.jkn.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_ticket")
public class QueueTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "queue_counter_id", nullable = false)
    private Long queueCounterId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "faskes_id", nullable = false)
    private Long faskesId;

    @Column(name = "tanggal", nullable = false)
    private LocalDate tanggal;

    @Column(name = "ticket_number", nullable = false)
    private Integer ticketNumber;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public QueueTicket() {
    }

    public QueueTicket(Long queueCounterId, String userId, Long faskesId, LocalDate tanggal, Integer ticketNumber) {
        this.queueCounterId = queueCounterId;
        this.userId = userId;
        this.faskesId = faskesId;
        this.tanggal = tanggal;
        this.ticketNumber = ticketNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getQueueCounterId() { return queueCounterId; }
    public void setQueueCounterId(Long queueCounterId) { this.queueCounterId = queueCounterId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Long getFaskesId() { return faskesId; }
    public void setFaskesId(Long faskesId) { this.faskesId = faskesId; }
    
    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }
    
    public Integer getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(Integer ticketNumber) { this.ticketNumber = ticketNumber; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}
