package com.dipo.domain.usecase

import com.dipo.domain.entity.LocationPoint
import com.dipo.domain.entity.RunStats
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Чистый UC для расчёта метрик по полилинии.
 *
 * На вход: точки маршрута (хронологически), время старта и «текущее» время (или endTime).
 * На выход: суммарная дистанция, длительность, темп.
 *
 * Замечание:
 * - Алгоритм Haversine упрощён (без учёта высоты/фильтра по accuracy/скорости).
 *   Фильтры лучше накладывать в data/background перед записью.
 */
class ComputeStats {
    suspend operator fun invoke(
        sessionId: String,
        points: List<LocationPoint>,
        startTimeMillis: Long,
        nowTimeMillis: Long
    ): Result<RunStats> = runCatching {
        val distance = totalDistanceMeters(points)
        val duration = (nowTimeMillis - startTimeMillis).coerceAtLeast(0L)
        val paceSecPerKm = if (distance > 1.0) {
            val sec = duration / 1000.0
            (sec / (distance / 1000.0)).roundToInt()
        } else 0

        RunStats(
            sessionId = sessionId,
            distanceMeters = distance,
            durationMillis = duration,
            paceSecPerKm = paceSecPerKm
        )
    }

    private fun totalDistanceMeters(points: List<LocationPoint>): Double {
        if (points.size < 2) return 0.0
        var sum = 0.0
        for (i in 1 until points.size) {
            sum += haversine(points[i - 1].lat, points[i - 1].lon, points[i].lat, points[i].lon)
        }
        return sum
    }

    /** Haversine: дистанция между двумя координатами на сфере (метры). */
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // радиус Земли, м
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                        sin(dLon / 2).pow(2.0))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}