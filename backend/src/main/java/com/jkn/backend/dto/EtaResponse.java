package com.jkn.backend.dto;

public class EtaResponse {
    private int etaMinutes;
    private long avgServiceSeconds;

    public EtaResponse(int etaMinutes, long avgServiceSeconds) {
        this.etaMinutes = etaMinutes;
        this.avgServiceSeconds = avgServiceSeconds;
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
