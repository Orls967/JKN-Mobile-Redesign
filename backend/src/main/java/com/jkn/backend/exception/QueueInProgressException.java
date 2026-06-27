package com.jkn.backend.exception;

public class QueueInProgressException extends RuntimeException {
    
    private final boolean retryable;
    private final int retryAfter;

    public QueueInProgressException(String message, boolean retryable, int retryAfter) {
        super(message);
        this.retryable = retryable;
        this.retryAfter = retryAfter;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public int getRetryAfter() {
        return retryAfter;
    }
}
