package com.jkn.mobile.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jkn.mobile.data.model.QueueResponse
import com.jkn.mobile.data.repository.QueueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QueueUiState(
    val isLoading: Boolean = false,
    val queue: QueueResponse? = null,
    val errorMessage: String? = null
)

class QueueViewModel : ViewModel() {

    private val repository = QueueRepository()

    private val _uiState = MutableStateFlow(QueueUiState())
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()

    fun fetchQueue(id: Long) {
        viewModelScope.launch {
            _uiState.value = QueueUiState(isLoading = true)

            val result = repository.getQueueById(id)

            result.onSuccess { queue ->
                Log.d("QueueViewModel", "Queue fetched successfully: $queue")
                _uiState.value = QueueUiState(queue = queue)
            }.onFailure { error ->
                Log.e("QueueViewModel", "Error fetching queue", error)
                _uiState.value = QueueUiState(errorMessage = error.message ?: "Unknown error")
            }
        }
    }
}
