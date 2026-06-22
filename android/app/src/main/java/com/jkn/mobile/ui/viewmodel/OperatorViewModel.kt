package com.jkn.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jkn.mobile.data.model.QueueResponse
import com.jkn.mobile.data.repository.QueueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class OperatorUiState(
    val availableQueues: List<QueueResponse> = emptyList(),
    val selectedQueueId: Long? = null,
    val currentNumber: String = "-",
    val nextNumber: String = "-", // TAMBAHAN BARU: Menyimpan Nomor Berikutnya
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class OperatorViewModel : ViewModel() {
    private val repository = QueueRepository()

    private val _uiState = MutableStateFlow(OperatorUiState())
    val uiState: StateFlow<OperatorUiState> = _uiState.asStateFlow()

    init {
        fetchQueues()
    }

    private fun fetchQueues() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getQueues()

            result.onSuccess { queues ->
                _uiState.update { it.copy(availableQueues = queues, isLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat daftar poli") }
            }
        }
    }

    fun selectQueue(queue: QueueResponse) {
        _uiState.update {
            it.copy(
                selectedQueueId = queue.id,
                currentNumber = queue.currentNumber.toString(),
                nextNumber = queue.nextNumber.toString(), // TAMBAHAN: Update saat poli dipilih
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun onNextClicked() {
        val queueId = _uiState.value.selectedQueueId ?: return

        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }

            val result = repository.nextQueue(queueId)

            result.onSuccess { updatedQueue ->
                _uiState.update {
                    it.copy(
                        currentNumber = updatedQueue.currentNumber.toString(),
                        nextNumber = updatedQueue.nextNumber.toString(), // TAMBAHAN: Update saat tombol Next ditekan
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                val errorMsg = if (error is HttpException && error.code() == 409) {
                    "Antrean sudah habis"
                } else {
                    "Gagal terhubung ke server"
                }
                _uiState.update { it.copy(isLoading = false, infoMessage = errorMsg) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }
}