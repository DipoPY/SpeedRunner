package com.dipo.background.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val CHANNEL_ID = "run_tracking"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = NotificationManagerCompat.from(context)
            val exists = mgr.notificationChannels.any { it.id == CHANNEL_ID }
            if (!exists) {
                val chan = NotificationChannel(
                    CHANNEL_ID,
                    "Пробежка",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Уведомления фонового трекинга"
                    setShowBadge(false)
                }
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(chan)
            }
        }
    }
} 