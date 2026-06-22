package com.jkn.mobile.data.websocket

import android.util.Log
import com.google.gson.Gson
import com.jkn.mobile.data.model.QueueChangedEvent
import com.jkn.mobile.ui.viewmodel.ConnectionStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

class QueueWebSocketClient {

    private val stompClient = StompClient(OkHttpWebSocketClient())
    private val gson = Gson()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    fun calculateBackoffDelay(retryCount: Int, maxDelay: Long = 10000L): Long {
        return (1000L * (1 shl retryCount.coerceAtMost(10))).coerceAtMost(maxDelay)
    }

    fun connectAndSubscribe(queueId: Long): Flow<QueueChangedEvent> = flow {
        var retryCount = 0
        val maxDelay = 10000L

        while (true) {
            var session: StompSession? = null
            try {
                _connectionStatus.value = ConnectionStatus.CONNECTING
                val url = "ws://10.0.2.2:8080/ws-queue/websocket"
                session = stompClient.connect(url)
                
                _connectionStatus.value = ConnectionStatus.CONNECTED
                Log.d("STOMP", "Connected to Queue WebSocket")
                retryCount = 0 // reset retry count on successful connect

                val subscription = session.subscribeText("/topic/queue/$queueId")
                
                subscription.collect { message ->
                    try {
                        val event = gson.fromJson(message, QueueChangedEvent::class.java)
                        emit(event)
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        Log.e("STOMP", "Failed to parse JSON: $message", e)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                Log.e("STOMP", "WebSocket connection lost/failed", e)
                
                val delayMs = calculateBackoffDelay(retryCount, maxDelay)
                retryCount++
                Log.d("STOMP", "Retrying in $delayMs ms (attempt $retryCount)")
                
                // Delay will throw CancellationException if cancelled during wait
                delay(delayMs)
            } finally {
                // Ensure session is properly disconnected when loop restarts or flow is cancelled
                try {
                    session?.disconnect()
                    Log.d("STOMP", "Session disconnected in finally block")
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e("STOMP", "Error disconnecting session", e)
                }
            }
        }
    }
}