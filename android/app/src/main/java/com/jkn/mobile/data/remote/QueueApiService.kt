package com.jkn.mobile.data.remote

import com.jkn.mobile.data.model.ApiResponse
import com.jkn.mobile.data.model.EtaResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QueueApiService {

    @GET("api/queues/{id}/eta")
    suspend fun getEta(
        @Path("id") id: Long,
        @Query("targetNumber") targetNumber: Int
    ): ApiResponse<EtaResponse>
}
