package com.dipo.domain.usecase

import com.dipo.domain.entity.RunStats
import com.dipo.domain.repo.RunRepository
import kotlinx.coroutines.flow.Flow

/**
 * Наблюдать поток агрегированной статистики по активной сессии.
 * Удобно подписывать UI на изменения в реальном времени.
 */
class ObserveActiveSession(private val runRepository: RunRepository) {
    operator fun invoke(sessionId: String): Flow<RunStats> =
        runRepository.observeStats(sessionId)
}