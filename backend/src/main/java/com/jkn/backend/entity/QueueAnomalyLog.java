package com.jkn.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_anomaly_log")
public class QueueAnomalyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "anomaly_type", nullable = false, length = 50)
    private QueueAnomalyType type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "detected_at", nullable = false, updatable = false)
    private LocalDateTime detectedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public QueueAnomalyLog() {
    }

    public QueueAnomalyLog(QueueAnomalyType type, String description, LocalDateTime detectedAt) {
        this.type = type;
        this.description = description;
        this.detectedAt = detectedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QueueAnomalyType getType() { return type; }
    public void setType(QueueAnomalyType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
