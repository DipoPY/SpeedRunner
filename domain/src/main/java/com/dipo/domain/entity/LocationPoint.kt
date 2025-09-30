package com.dipo.domain.entity

data class LocationPoint(
    /** ID сессии, к которой относится точка. */
    val sessionId: String,
    /** Время фиксации точки в миллисекундах UTC. */
    val time: Long,
    /** Широта в градусах (WGS84). */
    val lat: Double,
    /** Долгота в градусах (WGS84). */
    val lon: Double,
    /** Декларируемая точность в метрах. */
    val accuracy: Float
)