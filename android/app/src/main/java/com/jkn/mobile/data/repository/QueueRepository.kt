package com.jkn.mobile.data.repository

import com.jkn.mobile.data.model.QueueResponse
import com.jkn.mobile.data.remote.RetrofitClient

class QueueRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun getQueueById(id: Long): Result<QueueResponse> {
        return try {
            val response = apiService.getQueueById(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun nextQueue(id: Long): Result<QueueResponse> {
        return try {
            val response = apiService.nextQueue(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
