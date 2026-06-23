package com.jkn.mobile.data.model

data class QueueChangedEvent(
    val queueId: Long,
    val currentNumber: Int,
    val nextNumber: Int,
    val timestamp: Long
)
