package com.jkn.mobile.data.remote

import com.jkn.mobile.data.model.QueueResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("api/queues/{id}")
    suspend fun getQueueById(@Path("id") id: Long): QueueResponse
}
