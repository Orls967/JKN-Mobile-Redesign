package com.jkn.mobile.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.jkn.mobile.data.model.QueueChangedEvent
import com.jkn.mobile.data.model.QueueProximityEvent
import com.jkn.mobile.data.model.QueueResponse
import com.jkn.mobile.data.remote.RetrofitClient
import com.jkn.mobile.data.remote.WebSocketManager
import com.jkn.mobile.data.repository.QueueRepository
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    val queue: QueueResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val myTicketNumber: Int = 75 // Centralized hardcoded ticket for testing
)

class QueueViewModel : ViewModel() {

    private val repository = QueueRepository()
    private val crashlytics = FirebaseCrashlytics.getInstance()

    private val apiService = RetrofitClient.apiService

    private val _uiState = MutableStateFlow(QueueUiState())
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()
    
    private var isWebSocketConnected = false
    private var hasReceivedProximityNotification = false

    // Event bus for one-time Proximity Notification
    private val _showProximityNotifEvent = MutableSharedFlow<Int>()
    val showProximityNotifEvent = _showProximityNotifEvent.asSharedFlow()

    // State untuk menyimpan ETA
    private val _etaMinutes = MutableStateFlow<Int?>(null)
    val etaMinutes: StateFlow<Int?> = _etaMinutes.asStateFlow()

    fun fetchDefaultQueue() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val listResult = repository.getQueues()
            
            listResult.onSuccess { queues ->
                val queue = queues.firstOrNull()
                if (queue != null) {
                    _uiState.value = _uiState.value.copy(
                        queue = queue,
                        isLoading = false
                    )
                    Log.d("QueueViewModel", "Queue fetched successfully: $queue")

                    // Start WebSocket connection after REST fetch succeeds
                    connectWebSocket(queue.id)

                    fetchEta(queue.id, _uiState.value.myTicketNumber)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Tidak ada antrean yang tersedia"
                    )
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to fetch queues"
                )
                Log.e("QueueViewModel", "Fetch error", e)
                // Story 1.5 — Record REST fetch failure to Crashlytics
                crashlytics.log("REST fetchQueues failed")
                crashlytics.recordException(e)
            }
        }
    }

    private fun connectWebSocket(id: Long) {
        if (isWebSocketConnected) return
        isWebSocketConnected = true

        viewModelScope.launch {
            var retryDelay = 1000L
            val maxDelay = 5000L
            var retryCount = 0
            while (isActive) {
                try {
                    _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.CONNECTING)
                    // Story 1.5 — Log WebSocket connection attempt
                    crashlytics.log("WebSocket connecting to ${WebSocketManager.WS_URL} (attempt #${retryCount + 1})")
                    val session = WebSocketManager.stompClient.connect(WebSocketManager.WS_URL)
                    _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.CONNECTED)
                    retryDelay = 1000L // Reset delay on successful connection
                    retryCount = 0
                    // Story 1.5 — Log successful WebSocket connection
                    crashlytics.log("WebSocket connected successfully")
                    // Launch 1: Standard QueueChangedEvent Listener
                    launch {
                        try { // <--- PERBAIKAN: Tambahan Try-Catch di sini
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

                                    fetchEta(id, _uiState.value.myTicketNumber)

                                } catch (e: Exception) {
                                    Log.e("QueueViewModel", "Failed to parse JSON: $payload", e)
                                    // Story 1.5 — Record subscription JSON parse failure
                                    crashlytics.log("STOMP subscription parse failure on /topic/queue/$id")
                                    crashlytics.recordException(
                                        Exception("STOMP Subscription Parse Failure [queue/$id]", e)
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("QueueViewModel", "Koneksi baca WebSocket 1 terputus", e)
                        }
                    }

                    // Launch 2: Smart Proximity Listener
                    launch {
                        try { // <--- PERBAIKAN: Tambahan Try-Catch di sini
                            val proxSubscription = session.subscribeText("/topic/queue/$id/proximity")
                            proxSubscription.collect { payload ->
                                try {
                                    val event = Gson().fromJson(payload, QueueProximityEvent::class.java)
                                    if (event.patientNumber == _uiState.value.myTicketNumber && !hasReceivedProximityNotification) {
                                        hasReceivedProximityNotification = true
                                        _showProximityNotifEvent.emit(event.remainingQueue)
                                    }
                                } catch (e: Exception) {
                                    Log.e("QueueViewModel", "Failed to parse Proximity JSON: $payload", e)
                                    // Story 1.5 — Record proximity subscription parse failure
                                    crashlytics.log("STOMP subscription parse failure on /topic/queue/$id/proximity")
                                    crashlytics.recordException(
                                        Exception("STOMP Subscription Parse Failure [proximity]", e)
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("QueueViewModel", "Koneksi baca WebSocket 2 terputus", e)
                        }
                    }

                    // Await closure manually if connection drops
                    awaitCancellation()

                } catch (e: Exception) {
                    retryCount++
                    Log.e("QueueViewModel", "WebSocket disconnected", e)
                    _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.RECONNECTING)
                    // Story 1.5 — Record WebSocket connection/reconnect failure
                    crashlytics.log("WebSocket reconnect attempt #$retryCount failed: ${e.message}")
                    crashlytics.recordException(
                        Exception("WebSocket Connection Failure (attempt #$retryCount)", e)
                    )
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
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Unknown error"
                )
                // Story 1.5 — Record REST nextQueue failure to Crashlytics
                crashlytics.log("REST nextQueue failed for id=$id")
                crashlytics.recordException(error)
            }
        }
    }

    fun fetchEta(queueId: Long, ticketNumber: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.getQueueEta(queueId, ticketNumber)
                if (response.isSuccessful) {
                    _etaMinutes.value = response.body()
                }
            } catch (e: Exception) {
                // Biarkan kosong atau log error jika gagal, agar tidak mengganggu antrean utama
                Log.e("QueueViewModel", "Gagal memuat ETA: ${e.message}")
            }
        }
    }

}


