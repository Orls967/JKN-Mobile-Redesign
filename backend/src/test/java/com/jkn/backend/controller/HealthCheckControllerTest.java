package com.jkn.backend.controller;

import com.jkn.backend.service.HealthCheckService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthCheckController.class)
class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthCheckService healthCheckService;

    @Test
    void getHealth_whenHealthy_returns200() throws Exception {
        Map<String, Object> healthResult = new LinkedHashMap<>();
        healthResult.put("status", "healthy");
        healthResult.put("version", "0.0.1-SNAPSHOT");
        healthResult.put("uptime_seconds", 12345L);

        Map<String, Object> checks = new LinkedHashMap<>();
        Map<String, Object> dbCheck = Map.of("status", "ok", "latency_ms", 5L);
        checks.put("database", dbCheck);
        healthResult.put("checks", checks);

        when(healthCheckService.getFullHealth()).thenReturn(healthResult);

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.version").value("0.0.1-SNAPSHOT"))
                .andExpect(jsonPath("$.checks.database.status").value("ok"));
    }

    @Test
    void getHealth_whenUnhealthy_returns503() throws Exception {
        Map<String, Object> healthResult = new LinkedHashMap<>();
        healthResult.put("status", "unhealthy");
        healthResult.put("version", "0.0.1-SNAPSHOT");

        when(healthCheckService.getFullHealth()).thenReturn(healthResult);

        mockMvc.perform(get("/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("unhealthy"));
    }

    @Test
    void getHealthReady_whenReady_returns200() throws Exception {
        when(healthCheckService.isReady()).thenReturn(true);

        mockMvc.perform(get("/health/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ready"));
    }

    @Test
    void getHealthReady_whenNotReady_returns503() throws Exception {
        when(healthCheckService.isReady()).thenReturn(false);

        mockMvc.perform(get("/health/ready"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("not_ready"));
    }

    @Test
    void getHealthLive_returns200() throws Exception {
        when(healthCheckService.isAlive()).thenReturn(true);

        mockMvc.perform(get("/health/live"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("alive"));
    }
}
