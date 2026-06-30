package com.timerapp.linkb24.webdav

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

const val META_SUFFIX = ".sync-meta.json"

private val metaJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Serializable
data class RemoteSyncMeta(
    @SerialName("content_hash") val contentHash: String,
    val revision: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("device_id") val deviceId: String,
)

fun contentHash(payload: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(payload)
    return digest.joinToString("") { byte -> "%02x".format(byte) }
}

fun metaRemotePath(dataRemotePath: String): String {
    val path = dataRemotePath.trim()
    return if (path.endsWith(".json")) {
        path.dropLast(".json".length) + META_SUFFIX
    } else {
        path.trimEnd('/') + META_SUFFIX
    }
}

fun newMeta(payload: ByteArray, deviceId: String): RemoteSyncMeta {
    val digest = contentHash(payload)
    val now = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    return RemoteSyncMeta(
        contentHash = digest,
        revision = UUID.randomUUID().toString().replace("-", ""),
        updatedAt = now,
        deviceId = deviceId,
    )
}

fun parseMetaBytes(payload: ByteArray): RemoteSyncMeta? {
    return runCatching {
        metaJson.decodeFromString(RemoteSyncMeta.serializer(), payload.decodeToString())
    }.getOrNull()?.takeIf { it.contentHash.isNotBlank() }
}

fun metaToBytes(meta: RemoteSyncMeta): ByteArray {
    return metaJson.encodeToString(RemoteSyncMeta.serializer(), meta).encodeToByteArray()
}

fun remotePayloadHash(payload: ByteArray, meta: RemoteSyncMeta?): String {
    val fileHash = contentHash(payload)
    if (meta == null) {
        return fileHash
    }
    return if (meta.contentHash != fileHash) fileHash else meta.contentHash
}
