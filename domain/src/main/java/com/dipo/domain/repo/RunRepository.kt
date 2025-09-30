package com.dipo.domain.repo

import com.dipo.domain.entity.LocationPoint
import com.dipo.domain.entity.RunSession
import com.dipo.domain.entity.RunStats
import kotlinx.coroutines.flow.Flow

interface RunRepository {
    /**
     * Создаёт новую сессию пробежки и возвращает её ID.
     * @param startTime время старта в millis (по умолчанию — сейчас)
     */
    suspend fun createSession(startTime: Long = System.currentTimeMillis()): String

    /**
     * Закрывает существующую сессию (проставляет endTime и т.п.).
     */
    suspend fun closeSession(
        sessionId: String,
        endTime: Long = System.currentTimeMillis(),
        stats: RunStats? = null,
    )

    /**
     * Добавляет батч точек маршрута к соответствующим сессиям.
     * Предполагается вызов из фонового сервиса с периодическим накоплением.
     */
    suspend fun appendPoints(points: List<LocationPoint>)

    /**
     * Наблюдение за агрегированной статистикой активной сессии.
     * Источник — реализация может сводить свежие точки и кэш/БД.
     */
    fun observeStats(sessionId: String): Flow<RunStats>

    /**
     * История всех сессий в порядке убывания даты старта.
     * Используется для списка пробежек.
     */
    suspend fun getHistory(): List<RunSession>

    /**
     * Получение конкретной сессии (для экрана деталей).
     */
    suspend fun getById(sessionId: String): RunSession

    /**
     * Обновляет путь до превьюшки маршрута (PNG на диске),
     * когда её сгенерировал воркер.
     */
    suspend fun updatePreview(sessionId: String, path: String)

    /**
     * Полилиния (последовательность точек) сессии в хронологическом порядке.
     * Используется:
     *  - для расчёта метрик (дистанция/темп),
     *  - для отрисовки на нативной карте,
     *  - как сырьё для Static API превью.
     */
    suspend fun getPolyline(sessionId: String): List<LocationPoint>
}
