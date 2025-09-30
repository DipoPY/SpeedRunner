package com.dipo.domain.usecase

import com.dipo.domain.repo.RunRepository

/**
 * Завершить сессию пробежки (проставить endTime и зафиксировать агрегаты).
 */
class StopRunSession(
    private val runRepository: RunRepository,
    private val computeStats: ComputeStats,
) {
    suspend operator fun invoke(sessionId: String, endTime: Long = System.currentTimeMillis()) {
        val session = runRepository.getById(sessionId)
        val points = runRepository.getPolyline(sessionId)
        val stats = computeStats.invoke(sessionId, points, session.startTime, endTime)
        runRepository.closeSession(sessionId, endTime, stats.getOrNull())
    }
}
