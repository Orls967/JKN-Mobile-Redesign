package com.jkn.mobile.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.jkn.mobile.data.model.QueueChangedEvent
import com.jkn.mobile.data.model.QueueResponse
import com.jkn.mobile.data.remote.WebSocketManager
import com.jkn.mobile.data.repository.QueueRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.subscribeText

enum class ConnectionStatus {
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    DISCONNECTED
}

data class QueueUiState(
    val isLoading: Boolean = false,
    val queue: QueueResponse? = null,
    val errorMessage: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED
)

class QueueViewModel : ViewModel() {

    private val repository = QueueRepository()

    private val _uiState = MutableStateFlow(QueueUiState())
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()
    
    private var isWebSocketConnected = false

    fun fetchQueue(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = repository.getQueueById(id)

            result.onSuccess { queue ->
                Log.d("QueueViewModel", "Queue fetched successfully: $queue")
                _uiState.value = _uiState.value.copy(isLoading = false, queue = queue)
                
                // Initialize WebSocket connection after initial data load
                connectWebSocket(id)
            }.onFailure { error ->
                Log.e("QueueViewModel", "Error fetching queue", error)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message ?: "Unknown error")
            }
        }
    }

    private fun connectWebSocket(id: Long) {
        if (isWebSocketConnected) return
        isWebSocketConnected = true
        
        viewModelScope.launch {
            var retryDelay = 1000L
            while (isActive) {
                try {
                    _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.CONNECTING)
                    val session = WebSocketManager.stompClient.connect(WebSocketManager.WS_URL)
                    _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.CONNECTED)
                    retryDelay = 1000L // Reset delay on successful connection
                    
                    val subscription = session.subscribeText("/topic/queue/$id")
                    subscription.collect { payload ->
                        try {
                            val event = Gson().fromJson(payload, QueueChangedEvent::class.java)
                            _uiState.value = _uiState.value.copy(
                                queue = _uiState.value.queue?.copy(
                                    currentNumber = event.currentNumber,
                                    nextNumber = event.nextNumber
                                )
                            )
                            Log.d("QueueViewModel", "Received realtime update: $event")
                        } catch (e: Exception) {
                            Log.e("QueueViewModel", "Failed to parse JSON: $payload", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("QueueViewModel", "WebSocket disconnected", e)
                    _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.RECONNECTING)
                    delay(retryDelay)
                    retryDelay = (retryDelay * 2).coerceAtMost(5000L) // Exponential backoff max 5s
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
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Unknown error"
                )
            }
        }
    }
}
