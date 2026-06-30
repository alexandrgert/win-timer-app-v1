package com.timerapp.linkb24.webdav

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner

object AppForeground {
    fun isAtLeastStarted(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }
}
