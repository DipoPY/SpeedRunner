package com.dipo.domain.usecase

import com.dipo.domain.entity.LocationPoint
import com.dipo.domain.repo.RunRepository

/**
 * Батчево добавить точки маршрута (из фонового сервиса).
 * Реализация репозитория должна обеспечить транзакционность/оптимизацию вставок.
 */
class AppendLocationPoint(private val runRepository: RunRepository) {
    suspend operator fun invoke(points: List<LocationPoint>) {
        runRepository.appendPoints(points)
    }
}