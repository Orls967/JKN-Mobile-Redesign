package com.jkn.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkn.backend.dto.QueueStatusResponse;
import com.jkn.backend.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class QueueStatusHandlerTest {

    private QueueController queueController;
    private QueueService queueService;
    private com.jkn.backend.service.QueueEtaService queueEtaService;

    @BeforeEach
    void setUp() {
        queueService = Mockito.mock(QueueService.class);
        queueEtaService = Mockito.mock(com.jkn.backend.service.QueueEtaService.class);
        queueController = new QueueController(queueService, queueEtaService);
    }

    @Test
    void testGetQueueStatus_Completed() {
        QueueStatusResponse mockResponse = new QueueStatusResponse();
        mockResponse.setStatus("COMPLETED");
        mockResponse.setQueueNumber("A-042");
        mockResponse.setPoli("Poli A");
        
        when(queueService.getQueueStatusByIdempotencyKey("key-123")).thenReturn(mockResponse);

        ResponseEntity<QueueStatusResponse> response = queueController.getQueueStatus("key-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("COMPLETED", response.getBody().getStatus());
        assertEquals("A-042", response.getBody().getQueueNumber());
    }

    @Test
    void testGetQueueStatus_Processing() {
        QueueStatusResponse mockResponse = new QueueStatusResponse("PROCESSING", "Tunggu 2 detik", 2);
        
        when(queueService.getQueueStatusByIdempotencyKey("key-456")).thenReturn(mockResponse);

        ResponseEntity<QueueStatusResponse> response = queueController.getQueueStatus("key-456");

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("PROCESSING", response.getBody().getStatus());
        assertEquals(2, response.getBody().getRetryAfter());
    }
}
