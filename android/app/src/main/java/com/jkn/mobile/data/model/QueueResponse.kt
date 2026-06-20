package com.jkn.mobile.data.model

import com.google.gson.annotations.SerializedName

data class QueueResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("counterName")
    val counterName: String,

    @SerializedName("currentNumber")
    val currentNumber: Int,

    @SerializedName("nextNumber")
    val nextNumber: Int,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)
