package com.jkn.backend.controller;

import com.jkn.backend.dto.QueueResponse;
import com.jkn.backend.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.jkn.backend.service.QueueEtaService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QueueControllerTest {

    private MockMvc mockMvc;

    @Mock
    private QueueService queueService;

    @Mock
    private QueueEtaService queueEtaService;

    private QueueController queueController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        queueController = new QueueController(queueService, queueEtaService);
        mockMvc = MockMvcBuilders.standaloneSetup(queueController).build();
    }

    @Test
    void nextQueue_shouldReturn200Ok() throws Exception {
        // Arrange
        QueueResponse mockResponse = new QueueResponse(1L, "Poli Umum", 6, 7, null, null);
        when(queueService.nextQueue(1L)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(put("/api/queues/1/next")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.currentNumber").value(6))
                .andExpect(jsonPath("$.data.nextNumber").value(7));
    }

    @Test
    void nextQueue_shouldReturn409Conflict_whenQueueIsExhausted() throws Exception {
        // Arrange
        when(queueService.nextQueue(1L)).thenThrow(new IllegalStateException("Antrean sudah habis"));

        // Act & Assert
        mockMvc.perform(put("/api/queues/1/next")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Antrean sudah habis"));
    }
}
