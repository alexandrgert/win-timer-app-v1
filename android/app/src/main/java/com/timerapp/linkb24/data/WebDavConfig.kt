package com.timerapp.linkb24.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.math.max
import com.timerapp.linkb24.webdav.DEFAULT_REMIND_LATER_MINUTES
import com.timerapp.linkb24.webdav.REMIND_LATER_MINUTES_CHOICES
import com.timerapp.linkb24.webdav.metaRemotePath

const val DEFAULT_WEBDAV_REMOTE_PATH = "tasktimer/data.json"

@Serializable
data class WebDavConfig(
    val enabled: Boolean = false,
    val url: String = "",
    val username: String = "",
    val password: String = "",
    @SerialName("remote_path") val remotePath: String = DEFAULT_WEBDAV_REMOTE_PATH,
    @SerialName("sync_on_startup") val syncOnStartup: Boolean = true,
    @SerialName("sync_on_shutdown") val syncOnShutdown: Boolean = true,
    @SerialName("shutdown_upload_only") val shutdownUploadOnly: Boolean = false,
    @SerialName("sync_interval_minutes") val syncIntervalMinutes: Int = 0,
    @SerialName("sync_remind_later_minutes") val syncRemindLaterMinutes: Int = DEFAULT_REMIND_LATER_MINUTES,
    @SerialName("last_sync_at") val lastSyncAt: String? = null,
    @SerialName("last_error") val lastError: String = "",
    @SerialName("device_id") val deviceId: String = "",
    @SerialName("last_remote_content_hash") val lastRemoteContentHash: String = "",
    @SerialName("last_sync_had_conflict") val lastSyncHadConflict: Boolean = false,
    @SerialName("pending_notice") val pendingNotice: String = "",
    @SerialName("pending_remote_hash") val pendingRemoteHash: String = "",
    @SerialName("pending_remote_remind_at") val pendingRemoteRemindAt: String? = null,
) {
    fun isConfigured(): Boolean = url.trim().isNotEmpty() && username.trim().isNotEmpty()

    fun remoteUrl(): String {
        val base = url.trim().trimEnd('/') + "/"
        val path = remotePath.trim().trimStart('/')
        return base + path
    }

    fun metaRemoteUrl(): String = metaRemotePath(remoteUrl())

    fun withDeviceId(): WebDavConfig {
        if (deviceId.isNotBlank()) {
            return this
        }
        return copy(deviceId = UUID.randomUUID().toString().replace("-", ""))
    }

    fun periodicSyncEnabled(): Boolean = enabled && syncIntervalMinutes > 0 && isConfigured()
}

const val MIN_ANDROID_PERIODIC_SYNC_MINUTES = 15

fun normalizeSyncIntervalMinutes(value: Int): Int {
    val clamped = value.coerceIn(0, 1440)
    if (clamped == 0) {
        return 0
    }
    return max(clamped, MIN_ANDROID_PERIODIC_SYNC_MINUTES)
}

fun validateWebDavConfig(config: WebDavConfig): String? {
    if (!config.enabled) {
        return null
    }
    if (config.url.trim().isEmpty()) {
        return "Укажите URL WebDAV"
    }
    if (config.username.trim().isEmpty()) {
        return "Укажите имя пользователя"
    }
    if (config.remotePath.trim().isEmpty()) {
        return "Укажите путь к файлу на сервере"
    }
    if (config.syncIntervalMinutes !in 0..1440) {
        return "Интервал синхронизации: от 0 до 1440 минут"
    }
    if (config.syncRemindLaterMinutes !in REMIND_LATER_MINUTES_CHOICES) {
        return "Интервал напоминания: 5, 10, 15, 30 или 60 минут"
    }
    return null
}
