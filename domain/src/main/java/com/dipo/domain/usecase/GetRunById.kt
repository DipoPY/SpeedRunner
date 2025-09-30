package com.dipo.domain.usecase

import com.dipo.domain.entity.RunSession
import com.dipo.domain.repo.RunRepository

/**
 * Получить одну сессию по ID (для экрана деталей).
 */
class GetRunById(private val runRepository: RunRepository) {
    suspend operator fun invoke(sessionId: String): RunSession = runRepository.getById(sessionId)
}