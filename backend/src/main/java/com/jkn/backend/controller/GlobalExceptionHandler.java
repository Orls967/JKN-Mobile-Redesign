package com.jkn.backend.controller;

import com.jkn.backend.config.RequestIdFilter;
import com.jkn.backend.dto.ErrorResponse;
import com.jkn.backend.exception.QueueInProgressException;
import com.jkn.backend.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.SocketTimeoutException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private String getRequestId(HttpServletRequest request) {
        Object reqId = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        return reqId != null ? reqId.toString() : "unknown-request";
    }

    @ExceptionHandler(QueueInProgressException.class)
    public ResponseEntity<ErrorResponse> handleQueueInProgress(QueueInProgressException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                "QUEUE_IN_PROGRESS",
                "Request sedang diproses. " + ex.getMessage(),
                true,
                ex.getRetryAfter(),
                getRequestId(request)
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                "QUEUE_ALREADY_EXISTS",
                ex.getMessage(),
                false,
                null,
                getRequestId(request)
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                "QUEUE_NOT_FOUND",
                ex.getMessage(),
                false,
                null,
                getRequestId(request)
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidRequest(Exception ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                "INVALID_REQUEST",
                "Validasi input gagal: " + ex.getMessage(),
                false,
                null,
                getRequestId(request)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({
            TransactionTimedOutException.class,
            QueryTimeoutException.class,
            SocketTimeoutException.class
    })
    public ResponseEntity<ErrorResponse> handleTimeoutException(Exception ex, HttpServletRequest request) {
        log.error("Server Timeout Exception occurred: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(
                "SERVER_TIMEOUT",
                "Request took too long to process. Please try again.",
                true,
                null,
                getRequestId(request)
        );
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseError(DataAccessException ex, HttpServletRequest request) {
        log.error("Database error occurred: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(
                "DATABASE_ERROR",
                "Kesalahan pada database. Silakan coba lagi.",
                true,
                null,
                getRequestId(request)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception: ", ex);
        ErrorResponse response = new ErrorResponse(
                "SERVICE_UNAVAILABLE",
                "Sistem sedang mengalami gangguan.",
                true,
                null,
                getRequestId(request)
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
