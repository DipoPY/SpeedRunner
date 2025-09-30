package com.dipo.domain.usecase

import com.dipo.domain.entity.RunSession
import com.dipo.domain.repo.RunRepository

/**
 * Получить историю всех сессий (для экрана списка).
 */
class GetRunHistory(private val runRepository: RunRepository) {
    suspend operator fun invoke(): List<RunSession> = runRepository.getHistory()
}