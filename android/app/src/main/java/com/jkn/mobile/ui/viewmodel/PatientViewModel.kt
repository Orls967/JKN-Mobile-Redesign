package com.jkn.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jkn.mobile.data.websocket.QueueWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import android.content.Context
import com.jkn.mobile.utils.NotificationHelper

data class PatientUiState(
    val myNumber: Int = 0,
    val currentNumber: String = "-",
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val queueId: Long = 1L, // Default queue poli
    val notificationSent: Boolean = false
)

class PatientViewModel : ViewModel() {
    private val webSocketClient = QueueWebSocketClient()

    private val _uiState = MutableStateFlow(PatientUiState())
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()

    init {
        // Observe connection status updates
        viewModelScope.launch {
            webSocketClient.connectionStatus.collect { status ->
                _uiState.update { it.copy(connectionStatus = status) }
            }
        }
    }

    fun startObserving(context: Context) {
        // Start WebSocket connection and collect real-time updates
        viewModelScope.launch {
            webSocketClient.connectAndSubscribe(_uiState.value.queueId).collect { event ->
                _uiState.update { 
                    it.copy(currentNumber = event.currentNumber.toString()) 
                }

                val currentNum = event.currentNumber
                val myNum = _uiState.value.myNumber
                val remaining = myNum - currentNum

                if (remaining in 1..3 && !_uiState.value.notificationSent) {
                    NotificationHelper.showProximityNotification(context, remaining)
                    _uiState.update { it.copy(notificationSent = true) }
                }
            }
        }
    }

    // Dipanggil saat pasien mengetik manual nomor antreannya di form UI
    fun setMyNumber(numberStr: String) {
        val number = numberStr.toIntOrNull() ?: 0
        _uiState.update { it.copy(myNumber = number, notificationSent = false) }
    }
}
