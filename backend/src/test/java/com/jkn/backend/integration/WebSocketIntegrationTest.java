package com.jkn.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    public void setup() {
        stompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);
    }

    @Test
    public void testMultiClientBroadcastAndTopicIsolation() throws Exception {
        // Topic untuk Poli Jantung (1) dan Poli Gigi (2)
        String topicQueue1 = "/topic/queue/115"; // Assuming 115 from DB state, wait! 
        // We should just create a new queue via REST API to ensure we have a valid queue ID for testing!
        
        // 1. Setup Data - Buat Antrean Baru
        RestTemplate restTemplate = new RestTemplate();
        String createUrl = "http://localhost:" + port + "/api/queues";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Idempotency-Key", "integration-test-" + System.currentTimeMillis());
        
        String requestBody = "{\"counterName\":\"Poli Integrasi\",\"userId\":\"test_user\",\"faskesId\":1}";
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        Map<String, Object> createResp = restTemplate.postForObject(createUrl, request, Map.class);
        assertNotNull(createResp);
        Map<String, Object> data = (Map<String, Object>) createResp.get("data");
        Integer queueId = (Integer) data.get("id");
        assertNotNull(queueId);

        String topicQueueTarget = "/topic/queue/" + queueId;
        String topicQueueOther = "/topic/queue/9999";

        // CountDownLatch untuk Client A & B
        CountDownLatch latch = new CountDownLatch(2);
        
        BlockingQueue<Map<String, Object>> clientAQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Map<String, Object>> clientBQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Map<String, Object>> clientCQueue = new LinkedBlockingQueue<>();

        String wsUrl = "ws://localhost:" + port + "/ws-queue";

        // Client A - Subscribe to Target Queue
        StompSession sessionA = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
        sessionA.subscribe(topicQueueTarget, new QueueStompFrameHandler(clientAQueue, latch));

        // Client B - Subscribe to Target Queue
        StompSession sessionB = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
        sessionB.subscribe(topicQueueTarget, new QueueStompFrameHandler(clientBQueue, latch));

        // Client C - Subscribe to OTHER Queue
        StompSession sessionC = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
        sessionC.subscribe(topicQueueOther, new QueueStompFrameHandler(clientCQueue, null));

        // Beri waktu sebentar agar STOMP subscribe ter-register di broker
        Thread.sleep(1000);

        // 2. Client D (REST) menekan NEXT
        String nextUrl = "http://localhost:" + port + "/api/queues/" + queueId + "/next";
        restTemplate.exchange(nextUrl, HttpMethod.PUT, new HttpEntity<>(headers), Map.class);

        // 3. Verifikasi
        // Tunggu maksimal 2 detik untuk Client A dan B menerima pesan
        boolean receivedInTime = latch.await(2, TimeUnit.SECONDS);
        assertTrue(receivedInTime, "Client A dan B harus menerima pesan dalam waktu kurang dari 2 detik");

        // Client A & B harus mendapatkan payload yang sama
        Map<String, Object> payloadA = clientAQueue.poll(1, TimeUnit.SECONDS);
        Map<String, Object> payloadB = clientBQueue.poll(1, TimeUnit.SECONDS);

        assertNotNull(payloadA, "Client A tidak menerima broadcast");
        assertNotNull(payloadB, "Client B tidak menerima broadcast");
        
        assertEquals(payloadA.get("queueId"), payloadB.get("queueId"));
        assertEquals(payloadA.get("currentNumber"), payloadB.get("currentNumber"));
        assertEquals(payloadA.get("nextNumber"), payloadB.get("nextNumber"));
        assertEquals(queueId, payloadA.get("queueId"));

        // Client C tidak boleh menerima apa-apa (Silent)
        Map<String, Object> payloadC = clientCQueue.poll(1, TimeUnit.SECONDS);
        assertNull(payloadC, "Client C menerima broadcast yang salah topic (Cross-talk terjadi!)");
    }

    private static class QueueStompFrameHandler implements StompFrameHandler {
        private final BlockingQueue<Map<String, Object>> queue;
        private final CountDownLatch latch;

        public QueueStompFrameHandler(BlockingQueue<Map<String, Object>> queue, CountDownLatch latch) {
            this.queue = queue;
            this.latch = latch;
        }

        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            return Map.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object payload) {
            System.out.println("Received STOMP Message: " + payload);
            queue.offer((Map<String, Object>) payload);
            if (latch != null) {
                latch.countDown();
            }
        }
    }
}
