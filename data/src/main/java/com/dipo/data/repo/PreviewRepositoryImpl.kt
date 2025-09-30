package com.dipo.data.repo

import android.content.Context
import com.dipo.data.net.YandexStaticApi
import com.dipo.domain.entity.LocationPoint
import com.dipo.domain.repo.PreviewRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.Locale

class PreviewRepositoryImpl(
    private val appContext: Context,
    private val okHttp: OkHttpClient,
    private val staticApi: YandexStaticApi,
    private val staticApiKey: String,
) : PreviewRepository {

    override suspend fun buildStaticUrl(points: List<LocationPoint>): String {
        val pairs = points.map { it.lon to it.lat } // (lon, lat)
        return staticApi.buildPolylineUrl(
            points = pairs,
            apiKey = staticApiKey
        )
    }

    override suspend fun downloadAndCache(url: String, fileName: String): String {
        val safeName = sanitizeFileName(fileName)
        val dir = File(appContext.cacheDir, "run_previews").apply { mkdirs() }
        val tmp = File(dir, "$safeName.part")
        val out = File(dir, safeName)

        val req = Request.Builder().url(url).build()
        okHttp.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("Static API HTTP ${resp.code}")
            val body = resp.body ?: throw IOException("Empty body")
            body.byteStream().use { input ->
                tmp.outputStream().use { output -> input.copyTo(output) }
            }
        }

        if (out.exists()) out.delete()
        if (!tmp.renameTo(out)) {
            tmp.delete()
            throw IOException("Rename failed")
        }
        return out.absolutePath
    }

    private fun sanitizeFileName(name: String): String =
        name.lowercase(Locale.US).replace(Regex("[^a-z0-9._-]"), "_")
}