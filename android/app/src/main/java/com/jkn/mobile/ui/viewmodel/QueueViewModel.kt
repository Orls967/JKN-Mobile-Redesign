package com.jkn.mobile.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.jkn.mobile.data.model.QueueChangedEvent
import com.jkn.mobile.data.model.QueueResponse
import com.jkn.mobile.data.remote.WebSocketManager
import com.jkn.mobile.data.repository.QueueRepository
import com.jkn.mobile.utils.NotificationHelper
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.subscribeText

enum class ConnectionStatus {
    CONNECTING, CONNECTED, RECONNECTING, DISCONNECTED
}

data class QueueUiState(
    val queue: QueueResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val myTicketNumber: Int = 75
)

class QueueViewModel : ViewModel() {

    private val repository = QueueRepository()
    private val crashlytics = FirebaseCrashlytics.getInstance()

    private val _uiState = MutableStateFlow(QueueUiState())
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()

    private var isWebSocketConnected = false

    // Kunci anti-spam notifikasi
    private var hasNotifiedCall = false

    fun fetchQueue(id: Long, context: Context) {
        val appContext = context.applicationContext

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getQueueById(id)

            result.onSuccess { queue ->
                _uiState.value = _uiState.value.copy(queue = queue, isLoading = false)
                Log.d("QueueViewModel", "Queue fetched successfully: $queue")

                connectWebSocket(id, appContext)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Failed to fetch queue")
                Log.e("QueueViewModel", "Fetch error", e)
                crashlytics.log("REST fetchQueue failed for id=$id")
                crashlytics.recordException(e)
            }
        }
    }

    private fun connectWebSocket(id: Long, context: Context) {
        if (isWebSocketConnected) return
        isWebSocketConnected = true

        viewModelScope.launch {
            var retryDelay = 1000L
            val maxDelay = 5000L
            var retryCount = 0
            while (isActive) {
                try {
                    _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.CONNECTING)
                    crashlytics.log("WebSocket connecting to ${WebSocketManager.WS_URL} (attempt #${retryCount + 1})")
                    val session = WebSocketManager.stompClient.connect(WebSocketManager.WS_URL)
                    _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.CONNECTED)
                    retryDelay = 1000L
                    retryCount = 0
                    crashlytics.log("WebSocket connected successfully")

                    // HANYA BUTUH 1 LISTENER: Lebih cepat, ringan, dan sesuai Jira!
                    launch {
                        try {
                            val subscription = session.subscribeText("/topic/queue/$id")
                            subscription.collect { payload ->
                                try {
                                    val event = Gson().fromJson(payload, QueueChangedEvent::class.java)
                                    _uiState.value = _uiState.value.copy(
                                        queue = _uiState.value.queue?.copy(
                                            currentNumber = event.currentNumber,
                                            nextNumber = event.nextNumber,
                                            updatedAt = event.timestamp.toString()
                                        )
                                    )
                                    Log.d("QueueViewModel", "Received realtime update: $event")

                                    val currentNum = event.currentNumber
                                    val myNum = _uiState.value.myTicketNumber

                                    // 1. Notifikasi Panggilan Utama
                                    if (currentNum == myNum && !hasNotifiedCall) {
                                        hasNotifiedCall = true
                                        NotificationHelper.showQueueNotification(context, myNum)
                                        crashlytics.log("Queue called notification triggered")
                                    }

                                } catch (e: Exception) {
                                    Log.e("QueueViewModel", "Failed to parse JSON: $payload", e)
                                    crashlytics.log("STOMP subscription parse failure on /topic/queue/$id")
                                    crashlytics.recordException(Exception("STOMP Subscription Parse Failure [queue/$id]", e))
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("QueueViewModel", "Koneksi baca WebSocket terputus", e)
                        }
                    }

                    awaitCancellation()

                } catch (e: Exception) {
                    retryCount++
                    Log.e("QueueViewModel", "WebSocket disconnected", e)
                    _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.RECONNECTING)
                    crashlytics.log("WebSocket reconnect attempt #$retryCount failed: ${e.message}")
                    crashlytics.recordException(Exception("WebSocket Connection Failure (attempt #$retryCount)", e))
                    delay(retryDelay)
                    retryDelay = (retryDelay * 2).coerceAtMost(maxDelay)
                }
            }
        }
    }

    fun nextQueue(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.nextQueue(id)
            result.onSuccess { queue ->
                Log.d("QueueViewModel", "Queue advanced successfully: $queue")
                _uiState.value = _uiState.value.copy(isLoading = false, queue = queue)
            }.onFailure { error ->
                Log.e("QueueViewModel", "Error advancing queue", error)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message ?: "Unknown error")
                crashlytics.log("REST nextQueue failed for id=$id")
                crashlytics.recordException(error)
            }
        }
    }
}