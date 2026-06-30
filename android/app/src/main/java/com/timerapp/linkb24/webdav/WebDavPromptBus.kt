package com.timerapp.linkb24.webdav

import com.timerapp.linkb24.ui.RemoteChangePrompt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object WebDavPromptBus {
    private val _pending = MutableStateFlow<RemoteChangePrompt?>(null)
    val pending: StateFlow<RemoteChangePrompt?> = _pending.asStateFlow()

    fun offer(prompt: RemoteChangePrompt) {
        _pending.value = prompt
    }

    fun clear() {
        _pending.value = null
    }
}
