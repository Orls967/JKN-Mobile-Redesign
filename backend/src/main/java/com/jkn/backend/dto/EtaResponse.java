package com.jkn.backend.dto;

public class EtaResponse {
    private Long queueId;
    private int targetNumber;
    private int etaMinutes;
    private long avgServiceSeconds;

    public EtaResponse(Long queueId, int targetNumber, int etaMinutes, long avgServiceSeconds) {
        this.queueId = queueId;
        this.targetNumber = targetNumber;
        this.etaMinutes = etaMinutes;
        this.avgServiceSeconds = avgServiceSeconds;
    }

    public Long getQueueId() {
        return queueId;
    }

    public void setQueueId(Long queueId) {
        this.queueId = queueId;
    }

    public int getTargetNumber() {
        return targetNumber;
    }

    public void setTargetNumber(int targetNumber) {
        this.targetNumber = targetNumber;
    }

    public int getEtaMinutes() {
        return etaMinutes;
    }

    public void setEtaMinutes(int etaMinutes) {
        this.etaMinutes = etaMinutes;
    }

    public long getAvgServiceSeconds() {
        return avgServiceSeconds;
    }

    public void setAvgServiceSeconds(long avgServiceSeconds) {
        this.avgServiceSeconds = avgServiceSeconds;
    }
}
