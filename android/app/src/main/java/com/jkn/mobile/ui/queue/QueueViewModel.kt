package com.jkn.mobile.ui.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jkn.mobile.data.model.QueueChangedEvent
import com.jkn.mobile.data.websocket.QueueWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QueueViewModel : ViewModel() {
    private val webSocketClient = QueueWebSocketClient()

    private val _queueState = MutableStateFlow<QueueChangedEvent?>(null)
    val queueState: StateFlow<QueueChangedEvent?> = _queueState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    init {
        viewModelScope.launch {
            webSocketClient.connectionStatus.collect { status ->
                _isConnected.value = (status == com.jkn.mobile.ui.viewmodel.ConnectionStatus.CONNECTED)
            }
        }
    }

    fun connectAndSubscribe(queueId: Long) {
        viewModelScope.launch {
            webSocketClient.connectAndSubscribe(queueId).collect { event ->
                _queueState.value = event
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Krossbow session is now automatically disconnected when viewModelScope is cancelled.
    }
}