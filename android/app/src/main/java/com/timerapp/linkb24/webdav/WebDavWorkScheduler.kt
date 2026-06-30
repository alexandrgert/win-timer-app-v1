package com.timerapp.linkb24.webdav

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.timerapp.linkb24.data.WebDavConfigRepository
import java.util.concurrent.TimeUnit
import kotlin.math.max

object WebDavWorkScheduler {
    private const val WORK_NAME = "webdav_periodic_check"
    private const val MIN_WORK_INTERVAL_MINUTES = 15L

    fun schedule(context: Context) {
        val appContext = context.applicationContext
        val config = WebDavConfigRepository(appContext).load()
        val workManager = WorkManager.getInstance(appContext)
        if (!config.periodicSyncEnabled()) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }
        val intervalMinutes = max(config.syncIntervalMinutes.toLong(), MIN_WORK_INTERVAL_MINUTES)
        val request = PeriodicWorkRequestBuilder<WebDavCheckWorker>(intervalMinutes, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
