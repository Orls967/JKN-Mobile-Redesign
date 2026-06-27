package com.jkn.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueueStatusResponse {

    private String status;
    
    @JsonProperty("queue_number")
    private String queueNumber;
    
    private String poli;
    
    private String faskes;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    private String message;
    
    @JsonProperty("retry_after")
    private Integer retryAfter;

    // Constructors
    public QueueStatusResponse() {}
    
    public QueueStatusResponse(String status, String message, Integer retryAfter) {
        this.status = status;
        this.message = message;
        this.retryAfter = retryAfter;
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQueueNumber() { return queueNumber; }
    public void setQueueNumber(String queueNumber) { this.queueNumber = queueNumber; }

    public String getPoli() { return poli; }
    public void setPoli(String poli) { this.poli = poli; }

    public String getFaskes() { return faskes; }
    public void setFaskes(String faskes) { this.faskes = faskes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getRetryAfter() { return retryAfter; }
    public void setRetryAfter(Integer retryAfter) { this.retryAfter = retryAfter; }
}
