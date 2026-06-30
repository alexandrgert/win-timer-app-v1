package com.timerapp.linkb24.webdav

import com.timerapp.linkb24.data.WebDavConfig
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

val REMIND_LATER_MINUTES_CHOICES = listOf(5, 10, 15, 30, 60)
const val DEFAULT_REMIND_LATER_MINUTES = 15

fun normalizeRemindLaterMinutes(value: Int): Int {
    return if (value in REMIND_LATER_MINUTES_CHOICES) value else DEFAULT_REMIND_LATER_MINUTES
}

fun shouldShowRemotePrompt(config: WebDavConfig, remoteHash: String): Boolean {
    val pending = config.pendingRemoteHash.trim()
    if (pending.isEmpty() || pending != remoteHash.trim()) {
        return true
    }
    val remindAtRaw = config.pendingRemoteRemindAt?.trim().orEmpty()
    if (remindAtRaw.isEmpty()) {
        return true
    }
    val remindAt = runCatching {
        OffsetDateTime.parse(remindAtRaw, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }.getOrNull() ?: return true
    return !OffsetDateTime.now().isBefore(remindAt)
}

fun WebDavConfig.clearPendingRemoteRemind(): WebDavConfig {
    return copy(
        pendingRemoteHash = "",
        pendingRemoteRemindAt = null,
    )
}

fun WebDavConfig.withPendingRemoteRemind(remoteHash: String): WebDavConfig {
    val minutes = normalizeRemindLaterMinutes(syncRemindLaterMinutes)
    val remindAt = OffsetDateTime.now().plusMinutes(minutes.toLong())
        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    return copy(
        pendingRemoteHash = remoteHash.trim(),
        pendingRemoteRemindAt = remindAt,
    )
}

fun WebDavConfig.prepareRemotePrompt(remoteHash: String): WebDavConfig {
    val pending = pendingRemoteHash.trim()
    return if (pending.isNotEmpty() && pending != remoteHash.trim()) {
        clearPendingRemoteRemind()
    } else {
        this
    }
}
