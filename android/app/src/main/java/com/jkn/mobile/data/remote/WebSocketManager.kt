package com.jkn.mobile.data.remote

import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import java.util.concurrent.TimeUnit

object WebSocketManager {
    private val okHttpClient = OkHttpClient.Builder()
        .pingInterval(10, TimeUnit.SECONDS)
        .build()

    val stompClient = StompClient(OkHttpWebSocketClient(okHttpClient))
    const val WS_URL = "ws://10.0.2.2:8080/ws-queue/websocket"
}
