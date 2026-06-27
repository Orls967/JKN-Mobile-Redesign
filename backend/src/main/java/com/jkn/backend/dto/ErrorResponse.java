package com.jkn.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean success = false;
    
    @JsonProperty("error_code")
    private String errorCode;
    
    private String message;
    
    private boolean retryable;
    
    @JsonProperty("retry_after")
    private Integer retryAfter;
    
    @JsonProperty("request_id")
    private String requestId;
    
    private String timestamp;

    public ErrorResponse() {
        this.timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }

    public ErrorResponse(String errorCode, String message, boolean retryable, Integer retryAfter, String requestId) {
        this.errorCode = errorCode;
        this.message = message;
        this.retryable = retryable;
        this.retryAfter = retryAfter;
        this.requestId = requestId;
        this.timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRetryable() { return retryable; }
    public void setRetryable(boolean retryable) { this.retryable = retryable; }

    public Integer getRetryAfter() { return retryAfter; }
    public void setRetryAfter(Integer retryAfter) { this.retryAfter = retryAfter; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
