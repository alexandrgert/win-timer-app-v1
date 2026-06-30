package com.timerapp.linkb24

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.timerapp.linkb24.data.TaskRepository
import com.timerapp.linkb24.data.WebDavConfigRepository
import com.timerapp.linkb24.webdav.WebDavPeriodicMonitor
import com.timerapp.linkb24.webdav.WebDavSync
import com.timerapp.linkb24.webdav.WebDavWorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TimerApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val webDavSync by lazy {
        WebDavSync(TaskRepository(this), WebDavConfigRepository(this))
    }
    private val webDavPeriodicMonitor by lazy {
        WebDavPeriodicMonitor(this)
    }

    override fun onCreate() {
        super.onCreate()
        WebDavWorkScheduler.schedule(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    webDavPeriodicMonitor.restart()
                }

                override fun onStop(owner: LifecycleOwner) {
                    webDavPeriodicMonitor.stop()
                    appScope.launch {
                        webDavSync.syncOnShutdown()
                    }
                }
            },
        )
    }

    fun restartWebDavPeriodicMonitor() {
        WebDavWorkScheduler.schedule(this)
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            webDavPeriodicMonitor.restart()
        }
    }
}
