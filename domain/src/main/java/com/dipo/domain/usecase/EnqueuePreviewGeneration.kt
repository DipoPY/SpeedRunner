package com.dipo.domain.usecase

import com.dipo.domain.entity.LocationPoint
import com.dipo.domain.repo.PreviewRepository
import com.dipo.domain.repo.RunRepository

/**
 * Инициировать генерацию PNG-превью для сессии:
 * 1) достать полилинию из RunRepository,
 * 2) собрать URL статичной карты,
 * 3) скачать и сохранить PNG,
 * 4) обновить путь превью в RunRepository.
 *
 * Примечание:
 * - В проде это обычно делает WorkManager (в :background),
 *   но сам алгоритм чистый — его удобно выразить как UC.
 */
class EnqueuePreviewGeneration(
    private val runRepository: RunRepository,
    private val previewRepository: PreviewRepository
) {
    suspend operator fun invoke(sessionId: String): String {
        val points: List<LocationPoint> = runRepository.getPolyline(sessionId)
        val url = previewRepository.buildStaticUrl(points)
        val path = previewRepository.downloadAndCache(url, "$sessionId.png")
        runRepository.updatePreview(sessionId, path)
        return path
    }
}