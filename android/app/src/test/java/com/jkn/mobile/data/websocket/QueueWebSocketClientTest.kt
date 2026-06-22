package com.jkn.mobile.data.websocket

import org.junit.Assert.assertEquals
import org.junit.Test

class QueueWebSocketClientTest {

    private val client = QueueWebSocketClient()

    @Test
    fun testExponentialBackoffValues() {
        // Retry 0: 1000 * (1 shl 0) = 1000
        assertEquals(1000L, client.calculateBackoffDelay(0))
        
        // Retry 1: 1000 * (1 shl 1) = 2000
        assertEquals(2000L, client.calculateBackoffDelay(1))
        
        // Retry 2: 1000 * (1 shl 2) = 4000
        assertEquals(4000L, client.calculateBackoffDelay(2))
        
        // Retry 3: 1000 * (1 shl 3) = 8000
        assertEquals(8000L, client.calculateBackoffDelay(3))
        
        // Retry 4: 1000 * (1 shl 4) = 16000 -> capped at maxDelay (10000)
        assertEquals(10000L, client.calculateBackoffDelay(4, 10000L))
        
        // Retry 10: 1000 * (1 shl 10) = 1024000 -> capped at maxDelay (10000)
        assertEquals(10000L, client.calculateBackoffDelay(10, 10000L))
    }
}
