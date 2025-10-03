package com.dipo.background.worker

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.dipo.background.service.TrackingService

class TrackingWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return when (inputData.getString(KEY_ACTION)) {
            ACTION_START -> {
                startTracking()
                Result.success()
            }
            ACTION_STOP -> {
                stopTracking()
                Result.success()
            }
            else -> Result.failure()
        }
    }

    private fun startTracking() {
        val context = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            TrackingService.start(context)
        } else {
            context.startService(Intent(context, TrackingService::class.java).apply {
                action = TrackingService.ACTION_START
            })
        }
    }

    private fun stopTracking() {
        TrackingService.stop(applicationContext)
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "tracking_service_worker"
        private const val KEY_ACTION = "action"
        private const val ACTION_START = "start"
        private const val ACTION_STOP = "stop"

        fun enqueueStart(context: Context) {
            Log.d("gggg", "Worker: enqueueStart")
            enqueue(context, ACTION_START)
        }

        fun enqueueStop(context: Context) {
            Log.d("gggg", "Worker: enqueueStop")
            enqueue(context, ACTION_STOP)
        }

        private fun enqueue(context: Context, action: String) {
            val request = OneTimeWorkRequestBuilder<TrackingWorker>()
                .setInputData(workDataOf(KEY_ACTION to action))
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
