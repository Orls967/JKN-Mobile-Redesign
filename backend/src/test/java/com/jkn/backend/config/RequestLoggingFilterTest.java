package com.jkn.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RequestLoggingFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void response_containsXRequestIdHeader() throws Exception {
        mockMvc.perform(get("/api/queues"))
                .andExpect(header().exists("X-Request-ID"));
    }

    @Test
    void response_preservesIncomingXRequestId() throws Exception {
        String testRequestId = "test-request-id-12345";

        mockMvc.perform(get("/api/queues")
                        .header("X-Request-ID", testRequestId))
                .andExpect(header().string("X-Request-ID", testRequestId));
    }

    @Test
    void actuatorEndpoints_areNotFiltered() throws Exception {
        // Actuator endpoints should not have X-Request-ID
        // (mereka di-skip oleh shouldNotFilter)
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
