package com.timerapp.linkb24.data

import android.content.Context
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class WebDavConfigRepository(
    private val configFile: File,
) {
    constructor(context: Context) : this(File(context.filesDir, "webdav.json"))

    fun load(): WebDavConfig {
        if (!configFile.isFile) {
            return WebDavConfig().withDeviceId()
        }
        return runCatching {
            AppJson.decodeFromString(WebDavConfig.serializer(), configFile.readText())
        }.getOrElse {
            WebDavConfig().withDeviceId()
        }.withDeviceId()
    }

    fun save(config: WebDavConfig) {
        configFile.parentFile?.mkdirs()
        val normalized = config.withDeviceId()
        val payload = AppJson.encodeToString(WebDavConfig.serializer(), normalized)
        val tempFile = File(configFile.parentFile, "${configFile.name}.tmp")
        tempFile.writeText(payload)
        Files.move(
            tempFile.toPath(),
            configFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE,
        )
    }

    fun markSyncOk(config: WebDavConfig, remoteHash: String, hadConflict: Boolean) {
        val now = java.time.OffsetDateTime.now()
            .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        save(
            config.copy(
                lastSyncAt = now,
                lastError = "",
                lastRemoteContentHash = remoteHash,
                lastSyncHadConflict = hadConflict,
            ).withDeviceId(),
        )
    }

    fun markSyncError(config: WebDavConfig, message: String) {
        save(config.withDeviceId().copy(lastError = message.trim()))
    }

    fun savePendingNotice(message: String) {
        val config = load()
        save(config.copy(pendingNotice = message.trim()))
    }

    fun consumePendingNotice(): String {
        val config = load()
        val notice = config.pendingNotice.trim()
        if (notice.isNotEmpty()) {
            save(config.copy(pendingNotice = ""))
        }
        return notice
    }
}
