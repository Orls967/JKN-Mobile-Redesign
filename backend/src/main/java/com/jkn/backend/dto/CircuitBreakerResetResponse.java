package com.jkn.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CircuitBreakerResetResponse {

    private String dependency;
    
    @JsonProperty("previous_state")
    private String previousState;
    
    @JsonProperty("current_state")
    private String currentState;
    
    private String message;

    public CircuitBreakerResetResponse(String dependency, String previousState, String currentState, String message) {
        this.dependency = dependency;
        this.previousState = previousState;
        this.currentState = currentState;
        this.message = message;
    }

    public String getDependency() { return dependency; }
    public void setDependency(String dependency) { this.dependency = dependency; }

    public String getPreviousState() { return previousState; }
    public void setPreviousState(String previousState) { this.previousState = previousState; }

    public String getCurrentState() { return currentState; }
    public void setCurrentState(String currentState) { this.currentState = currentState; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
