package com.jkn.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterFilterTest {

    private RateLimiterFilter rateLimiterFilter;
    private RateLimitConfig rateLimitConfig;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        rateLimitConfig = new RateLimitConfig();
        rateLimitConfig.setLimit(5);
        rateLimitConfig.setWindowSeconds(60);
        
        objectMapper = new ObjectMapper();
        rateLimiterFilter = new RateLimiterFilter(rateLimitConfig, objectMapper);
    }

    @Test
    void testShouldNotFilterNonPostApiQueues() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/queues");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        rateLimiterFilter.doFilter(request, response, filterChain);

        // Filter should simply pass the request through without rate limiting headers
        assertNull(response.getHeader("X-RateLimit-Remaining"));
    }

    @Test
    void testRateLimitAllowsRequestsWithinLimit() throws Exception {
        for (int i = 0; i < 5; i++) {
            MockFilterChain filterChain = new MockFilterChain();
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/queues");
            request.addHeader("X-User-Id", "user123");
            MockHttpServletResponse response = new MockHttpServletResponse();
            
            rateLimiterFilter.doFilterInternal(request, response, filterChain);
            
            assertEquals("5", response.getHeader("X-RateLimit-Limit"));
            assertEquals(String.valueOf(5 - (i + 1)), response.getHeader("X-RateLimit-Remaining"));
        }
    }

    @Test
    void testRateLimitBlocksRequestsExceedingLimit() throws Exception {
        // Send 5 successful requests
        for (int i = 0; i < 5; i++) {
            MockFilterChain filterChain = new MockFilterChain();
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/queues");
            request.addHeader("X-User-Id", "user-abuse");
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimiterFilter.doFilterInternal(request, response, filterChain);
            assertEquals(200, response.getStatus()); // Mock response default
        }
        
        // 6th request should fail
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/queues");
        request.addHeader("X-User-Id", "user-abuse");
        MockHttpServletResponse response = new MockHttpServletResponse();
        rateLimiterFilter.doFilterInternal(request, response, filterChain);
        
        assertEquals(429, response.getStatus());
        assertEquals("0", response.getHeader("X-RateLimit-Remaining"));
        assertNotNull(response.getHeader("Retry-After"));
        assertTrue(response.getContentAsString().contains("RATE_LIMIT_EXCEEDED"));
    }
}
