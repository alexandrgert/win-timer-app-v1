package com.timerapp.linkb24.webdav

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.timerapp.linkb24.data.TaskRepository
import com.timerapp.linkb24.data.WebDavConfigRepository
import com.timerapp.linkb24.ui.RemoteChangePrompt

class WebDavCheckWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        if (AppForeground.isAtLeastStarted()) {
            return Result.success()
        }
        val configRepository = WebDavConfigRepository(applicationContext)
        val config = configRepository.load()
        if (!config.periodicSyncEnabled()) {
            return Result.success()
        }
        val check = WebDavSync(TaskRepository(applicationContext), configRepository)
            .checkRemoteChanges(config, requireEnabled = true)
        if (check.error.isNotBlank() || !check.remoteChanged) {
            return Result.success()
        }
        var updated = configRepository.load()
        if (!shouldShowRemotePrompt(updated, check.remoteHash)) {
            return Result.success()
        }
        updated = updated.prepareRemotePrompt(check.remoteHash)
        configRepository.save(updated)
        if (WebDavPromptBus.pending.value != null) {
            return Result.success()
        }
        WebDavNotificationHelper.showRemoteChange(
            applicationContext,
            RemoteChangePrompt(remoteHash = check.remoteHash),
        )
        return Result.success()
    }
}
