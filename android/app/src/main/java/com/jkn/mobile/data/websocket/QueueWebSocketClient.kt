package com.jkn.mobile.data.websocket

import android.util.Log
import com.google.gson.Gson
import com.jkn.mobile.data.model.QueueChangedEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribe
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

class QueueWebSocketClient {

    private val stompClient = StompClient(OkHttpWebSocketClient())
    private var stompSession: StompSession? = null
    private val gson = Gson()

    suspend fun connect() {
        try {
            val url = "ws://10.0.2.2:8080/ws-queue/websocket"
            stompSession = stompClient.connect(url)
            Log.d("STOMP", "Connected")
        } catch (e: Exception) {
            Log.e("STOMP", "Connection failed", e)
        }
    }

    suspend fun subscribeToQueue(queueId: Long): Flow<QueueChangedEvent> {
        val session = stompSession ?: return emptyFlow()

        return try {
            session.subscribe("/topic/queue/$queueId")
                .map { message ->
                    gson.fromJson(message.bodyAsText, QueueChangedEvent::class.java)
                }
                .catch { e ->
                    Log.e("STOMP", "Subscribe error", e)
                }
        } catch (e: Exception) {
            Log.e("STOMP", "Subscribe failed", e)
            emptyFlow()
        }
    }

    suspend fun disconnect() {
        stompSession?.disconnect()
        stompSession = null
    }
}