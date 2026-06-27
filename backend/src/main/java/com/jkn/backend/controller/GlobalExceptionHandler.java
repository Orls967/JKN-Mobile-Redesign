package com.jkn.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataAccessException;
import java.net.SocketTimeoutException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Menangkap Timeout Exceptions dari DB Query, Transaction, atau External Client
     * TASK-02-B: Timeout Matrix
     */
    @ExceptionHandler({
        TransactionTimedOutException.class, 
        QueryTimeoutException.class,
        SocketTimeoutException.class
    })
    public ResponseEntity<Map<String, Object>> handleTimeoutException(Exception ex) {
        log.error("Server Timeout Exception occurred: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error_code", "SERVER_TIMEOUT");
        errorResponse.put("retryable", true);
        errorResponse.put("message", "Request took too long to process. Please try again.");

        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(errorResponse);
    }
}
