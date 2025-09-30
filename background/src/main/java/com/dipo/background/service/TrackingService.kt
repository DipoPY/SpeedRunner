package com.dipo.background.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.dipo.domain.entity.LocationPoint
import com.dipo.domain.usecase.AppendLocationPoint
import com.dipo.domain.usecase.ComputeStats
import com.dipo.domain.usecase.EnqueuePreviewGeneration
import com.dipo.domain.usecase.StartRunSession
import com.dipo.domain.usecase.StopRunSession
import com.dipo.domain.repo.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/**
 * Foreground-сервис ведёт активную пробежку: держит подписку на локацию и не даёт процессу уснуть.
 */
class TrackingService : Service() {

    private val locationRepository: LocationRepository by inject()
    private val appendLocationPoint: AppendLocationPoint by inject()
    private val startRunSession: StartRunSession by inject()
    private val stopRunSession: StopRunSession by inject()
    private val computeStats: ComputeStats by inject()
    private val enqueuePreviewGeneration: EnqueuePreviewGeneration by inject()

    // Держим корутины в Main, чтобы все вызовы, влияющие на UI (startForeground), шли по главному потоку.
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var locationJob: Job? = null

    private var currentSessionId: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val existing = currentSessionId
                if (existing == null) {
                    serviceScope.launch {
                        val sessionId = startRunSession()
                        currentSessionId = sessionId
                        // Требование Android 8+: в течение 5 секунд показать постоянное уведомление.
                        startForeground(NOTIFICATION_ID, buildNotification(sessionId, 0.0, 0L, 0))
                        TrackingSessionState.activate(sessionId)
                        subscribeToLocations(sessionId)
                    }
                } else {
                    TrackingSessionState.activate(existing)
                    startForeground(NOTIFICATION_ID, buildNotification(existing, 0.0, 0L, 0))
                }
            }
            ACTION_STOP -> {
                val sessionId = currentSessionId
                if (sessionId != null) {
                   serviceScope.launch {
                       stopRunSession(sessionId)
                       // Тяжёлую пост-обработку лучше выносить в фон; пока вызываем напрямую ради простоты UX.
                       try { enqueuePreviewGeneration(sessionId) } catch (_: Throwable) {}
                       TrackingSessionState.clear()
                        currentSessionId = null
                        stopSelf()
                    }
                } else {
                    TrackingSessionState.clear()
                    currentSessionId = null
                    stopSelf()
                }
            }
        }
        // START_STICKY: система перезапустит сервис, если процесс выгрузят во время активной пробежки.
        return START_STICKY
    }

    override fun onDestroy() {
        TrackingSessionState.clear()
        locationJob?.cancel()
        super.onDestroy()
    }

    private fun subscribeToLocations(sessionId: String) {
        // На перезапуске сервиса отменяем прошлую подписку, иначе получим дубли потоков.
        locationJob?.cancel()
        locationJob = serviceScope.launch(Dispatchers.IO) {
            val points: MutableList<LocationPoint> = mutableListOf()
            val startTime = System.currentTimeMillis()
            locationRepository.locationFlow().collectLatest { rawPoint ->
                val point = rawPoint.copy(sessionId = sessionId)
                points.add(point)
                appendLocationPoint(listOf(point))

                val stats = computeStats.invoke(sessionId, points, startTime, System.currentTimeMillis())
                stats.getOrNull()?.let {
                    TrackingSessionState.update(
                        sessionId,
                        it.distanceMeters,
                        it.durationMillis
                    )


                    withContext(Dispatchers.Main) {
                        startForeground(
                            NOTIFICATION_ID,
                            buildNotification(
                                sessionId,
                                it.distanceMeters,
                                it.durationMillis,
                                it.paceSecPerKm
                            )
                        )
                    }
                }
            }
        }
    }

    private fun buildNotification(
        sessionId: String,
        distanceMeters: Double,
        durationMillis: Long,
        paceSecPerKm: Int
    ): Notification {
        val channelId = NotificationHelper.CHANNEL_ID
        NotificationHelper.ensureChannel(this)

        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(INTENT_OPEN).apply {
                setPackage(packageName)
                action = INTENT_OPEN
                putExtra(EXTRA_SESSION_ID, sessionId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, TrackingService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val minutes = durationMillis / 60_000
        val seconds = (durationMillis / 1_000) % 60
        val title = "Пробежка идёт"
        val text = "Дистанция: ${"%1$.2f".format(distanceMeters / 1000)} км · Время: ${"%d:%02d".format(minutes, seconds)} · Темп: ${if (paceSecPerKm > 0) paceSecPerKm else "—"} с/км"

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openIntent)
            .setOngoing(true) // Foreground-уведомление должно висеть, пока идёт работа.
            .addAction(android.R.drawable.ic_media_pause, "Стоп", stopIntent)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.dipo.background.action.START"
        const val ACTION_STOP = "com.dipo.background.action.STOP"
        const val INTENT_OPEN = "OPEN_MAP_SCREEN"
        const val EXTRA_SESSION_ID = "session_id"

        @RequiresApi(Build.VERSION_CODES.O)
        fun start(context: Context) {
            val i = Intent(context, TrackingService::class.java).apply { action = ACTION_START }
            context.startForegroundService(i)
        }

        fun stop(context: Context) {
            val i = Intent(context, TrackingService::class.java).apply { action = ACTION_STOP }
            context.startService(i)
        }
    }
}
