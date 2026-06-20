package com.jkn.mobile.data.model

data class ApiResponse<T>(
    val success: Boolean,
    val status: Int,
    val message: String,
    val data: T?,
    val timestamp: String
)
