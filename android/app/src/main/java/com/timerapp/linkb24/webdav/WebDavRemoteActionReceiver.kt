package com.timerapp.linkb24.webdav

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.timerapp.linkb24.data.TaskRepository
import com.timerapp.linkb24.data.WebDavConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WebDavRemoteActionReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) {
            return
        }
        val remoteHash = intent.getStringExtra(WebDavNotificationHelper.EXTRA_REMOTE_HASH).orEmpty()
        if (remoteHash.isBlank()) {
            return
        }
        val pendingResult = goAsync()
        scope.launch {
            try {
                when (intent.action) {
                    WebDavNotificationHelper.ACTION_CONFIRM -> confirmPull(context)
                    WebDavNotificationHelper.ACTION_LATER -> postpone(context, remoteHash)
                }
            } finally {
                WebDavNotificationHelper.cancel(context)
                pendingResult.finish()
            }
        }
    }

    private fun confirmPull(context: Context) {
        val configRepository = WebDavConfigRepository(context)
        val config = configRepository.load().clearPendingRemoteRemind()
        configRepository.save(config)
        WebDavSync(TaskRepository(context), configRepository).pullAndMerge(config, requireEnabled = false)
    }

    private fun postpone(context: Context, remoteHash: String) {
        val configRepository = WebDavConfigRepository(context)
        val config = configRepository.load().withPendingRemoteRemind(remoteHash)
        configRepository.save(config)
    }
}
