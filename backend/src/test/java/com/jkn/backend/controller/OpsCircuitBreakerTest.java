package com.jkn.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkn.backend.client.BpjsClient;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.jpa.hibernate.ddl-auto=update", "spring.flyway.enabled=false"})
@AutoConfigureMockMvc
class OpsCircuitBreakerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private BpjsClient bpjsClient;

    private static final String OPS_TOKEN = "ops-secret-token-123";

    @BeforeEach
    void setUp() {
        // Reset CB before each test
        circuitBreakerRegistry.circuitBreaker("bpjs_api").transitionToClosedState();
        bpjsClient.setSimulatedFailure(false);
    }

    @Test
    void testOpsEndpointUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/ops/circuit-breaker/status"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error_code").value("UNAUTHORIZED"));
    }

    @Test
    void testOpsEndpointAuthorized() throws Exception {
        mockMvc.perform(get("/ops/circuit-breaker/status")
                        .header("Authorization", "Bearer " + OPS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.circuits").isArray());
    }

    @Test
    void testCircuitBreakerStateTransition() throws Exception {
        bpjsClient.setSimulatedFailure(true);
        
        // Simulasikan 5 kegagalan
        for (int i = 0; i < 5; i++) {
            try {
                bpjsClient.checkBpjs("12345");
            } catch (Exception ignored) {
            }
        }
        
        // Setelah 5 kegagalan, circuit harus OPEN
        assert circuitBreakerRegistry.circuitBreaker("bpjs_api").getState().name().equals("OPEN");

        // Memanggil API reset
        mockMvc.perform(post("/ops/circuit-breaker/bpjs_api/reset")
                        .header("Authorization", "Bearer " + OPS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dependency").value("bpjs_api"))
                .andExpect(jsonPath("$.previous_state").value("OPEN"))
                .andExpect(jsonPath("$.current_state").value("HALF_OPEN"));
                
        // Verifikasi bahwa state benar-benar HALF_OPEN
        assert circuitBreakerRegistry.circuitBreaker("bpjs_api").getState().name().equals("HALF_OPEN");
    }
}
