package com.timerapp.linkb24.webdav

import android.content.Context
import com.timerapp.linkb24.data.TaskRepository
import com.timerapp.linkb24.data.WebDavConfigRepository
import com.timerapp.linkb24.ui.RemoteChangePrompt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebDavPeriodicMonitor(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val configRepository = WebDavConfigRepository(appContext)
    private val webDavSync = WebDavSync(TaskRepository(appContext), configRepository)
    private var loopJob: Job? = null

    fun restart() {
        loopJob?.cancel()
        loopJob = scope.launch {
            while (isActive) {
                val config = configRepository.load()
                if (!config.periodicSyncEnabled()) {
                    break
                }
                if (!AppForeground.isAtLeastStarted()) {
                    delay(5_000)
                    continue
                }
                delay(config.syncIntervalMinutes * 60_000L)
                if (!isActive || !AppForeground.isAtLeastStarted()) {
                    continue
                }
                performCheck()
            }
        }
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
    }

    fun performCheck() {
        scope.launch {
            val config = configRepository.load()
            if (!config.periodicSyncEnabled()) {
                return@launch
            }
            val check = withContext(Dispatchers.IO) {
                webDavSync.checkRemoteChanges(config, requireEnabled = true)
            }
            handleCheckResult(check)
        }
    }

    private suspend fun handleCheckResult(check: RemoteCheckOutcome) {
        if (check.error.isNotBlank() || !check.remoteChanged) {
            return
        }
        var config = configRepository.load()
        if (!shouldShowRemotePrompt(config, check.remoteHash)) {
            return
        }
        config = config.prepareRemotePrompt(check.remoteHash)
        configRepository.save(config)
        if (WebDavPromptBus.pending.value != null) {
            return
        }
        WebDavPromptBus.offer(RemoteChangePrompt(remoteHash = check.remoteHash))
    }
}
