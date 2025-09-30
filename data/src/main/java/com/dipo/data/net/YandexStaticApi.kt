package com.dipo.data.net

import android.annotation.SuppressLint
import android.net.Uri
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

const val BASE_ENDPOINT = "https://static-maps.yandex.ru/v1"

interface YandexStaticApi {
    fun buildPolylineUrl(
        points: List<Pair<Double, Double>>,
        markers: List<Triple<Double, Double, String>> = emptyList(),
        colorArgb: String = "FF22DDFF",
        lineWidthPx: Int = 5,
        size: Pair<Int, Int> = 600 to 400,
        lang: String = "ru_RU",
        bboxPaddingDeg: Double = 0.001,
        apiKey: String,
    ): String
}

class YandexStaticApiImpl : YandexStaticApi {

    override fun buildPolylineUrl(
        points: List<Pair<Double, Double>>,
        markers: List<Triple<Double, Double, String>>,
        colorArgb: String,
        lineWidthPx: Int,
        size: Pair<Int, Int>,
        lang: String,
        bboxPaddingDeg: Double,
        apiKey: String
    ): String {
        require(points.size >= 2) { "Polyline требует минимум 2 точки" }

        val plCoords = points.joinToString("~") { (lon, lat) -> "${lon.f6()},${lat.f6()}" }
        val pl = "c:$colorArgb,w:$lineWidthPx,$plCoords"

        val pt = if (markers.isNotEmpty()) {
            markers.joinToString("~") { (lon, lat, style) -> "${lon.f6()},${lat.f6()},$style" }
        } else null

        val (minLon, minLat, maxLon, maxLat) = bounds(points, bboxPaddingDeg)

        return Uri.parse(BASE_ENDPOINT).buildUpon()
            .appendQueryParameter("lang", lang)
            .appendQueryParameter("size", "${size.first},${size.second}")
            .appendQueryParameter("pl", pl)
            .appendQueryParameter("bbox", "${minLon.f6()},${minLat.f6()}~${maxLon.f6()},${maxLat.f6()}")
            .apply { if (pt != null) appendQueryParameter("pt", pt) }
            .appendQueryParameter("apikey", apiKey)
            .build()
            .toString()
    }

    private fun bounds(points: List<Pair<Double, Double>>, pad: Double): Quad {
        var minLon = Double.POSITIVE_INFINITY
        var maxLon = Double.NEGATIVE_INFINITY
        var minLat = Double.POSITIVE_INFINITY
        var maxLat = Double.NEGATIVE_INFINITY
        for ((lon, lat) in points) {
            minLon = min(minLon, lon)
            maxLon = max(maxLon, lon)
            minLat = min(minLat, lat)
            maxLat = max(maxLat, lat)
        }
        return Quad(minLon - pad, minLat - pad, maxLon + pad, maxLat + pad)
    }

    private fun Double.f6(): String = String.format(Locale.US, "%.6f", this)
    private data class Quad(val minLon: Double, val minLat: Double, val maxLon: Double, val maxLat: Double)
}