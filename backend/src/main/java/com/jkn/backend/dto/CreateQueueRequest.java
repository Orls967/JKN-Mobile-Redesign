package com.jkn.backend.dto;

public class CreateQueueRequest {

    private String counterName;
    private String userId;
    private Long faskesId;

    public CreateQueueRequest() {
    }

    public CreateQueueRequest(String counterName, String userId, Long faskesId) {
        this.counterName = counterName;
        this.userId = userId;
        this.faskesId = faskesId;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getFaskesId() {
        return faskesId;
    }

    public void setFaskesId(Long faskesId) {
        this.faskesId = faskesId;
    }
}
