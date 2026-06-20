package com.jkn.backend.dto;

public class CreateQueueRequest {

    private String counterName;

    public CreateQueueRequest() {
    }

    public CreateQueueRequest(String counterName) {
        this.counterName = counterName;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }
}
