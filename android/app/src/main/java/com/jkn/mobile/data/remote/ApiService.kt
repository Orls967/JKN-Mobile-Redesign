package com.jkn.mobile.data.remote

import com.jkn.mobile.data.model.ApiResponse
import com.jkn.mobile.data.model.QueueResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/queues")
    suspend fun getQueues(): ApiResponse<List<QueueResponse>>

    @GET("api/queues/{id}")
    suspend fun getQueueById(@Path("id") id: Long): ApiResponse<QueueResponse>

    @PUT("api/queues/{id}/next")
    suspend fun nextQueue(@Path("id") id: Long): ApiResponse<QueueResponse>
}
