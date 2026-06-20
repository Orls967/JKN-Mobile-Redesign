package com.jkn.mobile.data.model

data class QueueProximityEvent(
    val queueId: Long,
    val currentNumber: Int,
    val patientNumber: Int,
    val remainingQueue: Int,
    val timestamp: String
)
