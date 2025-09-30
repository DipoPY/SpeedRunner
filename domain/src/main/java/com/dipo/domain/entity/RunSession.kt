package com.dipo.domain.entity

data class RunSession(
    val id: String,
    /** Время старта в миллисекундах UTC. */
    val startTime: Long,
    /** Время окончания в миллисекундах UTC; null — сессия активна. */
    val endTime: Long? = null,
    /** Пробег за сессию в метрах. */
    val distanceMeters: Double = 0.0,
    /** Длительность в миллисекундах (endTime - startTime). */
    val durationMillis: Long = 0L,
    /**
     * Темп (секунд на километр).
     * 0 — если ещё не посчитан/недостаточно данных.
     */
    val paceSecPerKm: Int = 0,
    /**
     * Локальный путь к PNG-превью карты (генерируется воркером).
     * null — превью ещё не сгенерировано.
     */
    val previewPath: String? = null
)