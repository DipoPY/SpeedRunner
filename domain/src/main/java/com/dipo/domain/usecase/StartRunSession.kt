package com.dipo.domain.usecase

import com.dipo.domain.repo.RunRepository

/**
 * Создать новую сессию пробежки.
 * Возвращает ID созданной сессии.
 */
class StartRunSession(private val runRepository: RunRepository) {
    suspend operator fun invoke(startTime: Long = System.currentTimeMillis()): String =
        runRepository.createSession(startTime)
}