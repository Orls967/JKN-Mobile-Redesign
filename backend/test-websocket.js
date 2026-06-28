const { Client } = require('@stomp/stompjs');
const WebSocket = require('ws');
const SockJS = require('sockjs-client');

// The STOMP client configuration
const client = new Client({
    // We use SockJS if ws:// isn't directly exposed, but since we have .withSockJS() and ws support we can try the raw websocket first
    // Note: Spring Boot withSockJS() exposes a raw websocket endpoint at /ws-queue/websocket
    brokerURL: 'ws://localhost:8080/ws-queue/websocket',
    
    // Fallback if brokerURL fails, we can use webSocketFactory with SockJS
    // webSocketFactory: () => new SockJS('http://localhost:8080/ws-queue'),
    
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
});

// Configure WebSocket constructor for NodeJS environment
Object.assign(global, { WebSocket });

client.onConnect = function (frame) {
    console.log('✅ Connected to WebSocket Server!');
    console.log('📡 Subscribing to Topic: /topic/queue/115 ...');
    
    // Subscribe to Queue ID = 115 (Loket Pendaftaran)
    client.subscribe('/topic/queue/115', (message) => {
        console.log('\n======================================================');
        console.log('🔔 [REAL-TIME UPDATE RECEIVED]');
        console.log(`⏱️ Waktu: ${new Date().toISOString()}`);
        console.log('📦 Data Payload:');
        console.log(JSON.stringify(JSON.parse(message.body), null, 2));
        console.log('======================================================\n');
    });
    
    console.log('⌛ Waiting for Operator to click NEXT...');
};

client.onStompError = function (frame) {
    console.log('Broker reported error: ' + frame.headers['message']);
    console.log('Additional details: ' + frame.body);
};

console.log('Menghubungkan ke STOMP Server...');
client.activate();
