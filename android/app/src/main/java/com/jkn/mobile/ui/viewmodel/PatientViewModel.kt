package com.jkn.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.jkn.mobile.data.repository.QueueRepository
import com.jkn.mobile.data.websocket.QueueWebSocketClient
import com.jkn.mobile.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview

data class PatientUiState(
    val myNumber: Int = 0,
    val currentNumber: String = "-",
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val queueId: Long = 1L, // Default queue poli
    val notificationSent: Boolean = false,
    val etaMinutes: Int = -1 // -1 means unknown/loading
)

@OptIn(FlowPreview::class)
class PatientViewModel : ViewModel() {
    private val webSocketClient = QueueWebSocketClient()
    private val queueRepository = QueueRepository()

    private val _uiState = MutableStateFlow(PatientUiState())
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()

    private val _etaRequestFlow = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        // Observe connection status updates
        viewModelScope.launch {
            webSocketClient.connectionStatus.collect { status ->
                _uiState.update { it.copy(connectionStatus = status) }
            }
        }

        // Observe and debounce ETA requests
        viewModelScope.launch {
            _etaRequestFlow
                .debounce(2000L)
                .collect {
                    fetchEta()
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

                if (myNum > 0) {
                    _etaRequestFlow.tryEmit(Unit)
                }

                if (remaining in 1..3 && !_uiState.value.notificationSent) {
                    NotificationHelper.showProximityNotification(context, remaining)
                    _uiState.update { it.copy(notificationSent = true) }
                }
            }
        }
    }

    private fun fetchEta() {
        viewModelScope.launch {
            val myNum = _uiState.value.myNumber
            if (myNum <= 0) return@launch
            
            val result = queueRepository.getQueueEta(_uiState.value.queueId, myNum)
            result.onSuccess { etaResponse ->
                _uiState.update { it.copy(etaMinutes = etaResponse.etaMinutes) }
            }
        }
    }

    // Dipanggil saat pasien mengetik manual nomor antreannya di form UI
    fun setMyNumber(numberStr: String) {
        val number = numberStr.toIntOrNull() ?: 0
        _uiState.update { it.copy(myNumber = number, notificationSent = false) }
        if (number > 0) {
            _etaRequestFlow.tryEmit(Unit)
        }
    }
}
