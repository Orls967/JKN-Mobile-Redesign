package com.jkn.backend.dto;

import java.util.List;

public class CircuitBreakerStatusResponse {

    private List<CircuitBreakerInfo> circuits;

    public CircuitBreakerStatusResponse(List<CircuitBreakerInfo> circuits) {
        this.circuits = circuits;
    }

    public List<CircuitBreakerInfo> getCircuits() { return circuits; }
    public void setCircuits(List<CircuitBreakerInfo> circuits) { this.circuits = circuits; }

    public static class CircuitBreakerInfo {
        private String dependency;
        private String state;
        private double failure_rate;

        public CircuitBreakerInfo(String dependency, String state, double failure_rate) {
            this.dependency = dependency;
            this.state = state;
            this.failure_rate = failure_rate;
        }

        public String getDependency() { return dependency; }
        public void setDependency(String dependency) { this.dependency = dependency; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public double getFailure_rate() { return failure_rate; }
        public void setFailure_rate(double failure_rate) { this.failure_rate = failure_rate; }
    }
}
