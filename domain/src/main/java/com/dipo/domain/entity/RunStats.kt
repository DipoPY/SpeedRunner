package com.dipo.domain.entity

data class RunStats(
    val sessionId: String,
    val distanceMeters: Double,
    val durationMillis: Long,
    val paceSecPerKm: Int
)