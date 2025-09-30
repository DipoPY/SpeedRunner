package com.dipo.domain.repo

import com.dipo.domain.entity.LocationPoint

interface PreviewRepository {

    /**
     * Собирает URL/запрос для статичной карты по полилинии.
     * Логику оформления (цвет/толщина/размер) решает реализация.
     */
    suspend fun buildStaticUrl(points: List<LocationPoint>): String

    /**
     * Скачивает превью и сохраняет в локальный кеш (filesDir/cacheDir).
     * @param fileName имя файла (например, "<sessionId>.png")
     * @return абсолютный путь до сохранённого файла
     */
    suspend fun downloadAndCache(url: String, fileName: String): String
}