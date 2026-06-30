package com.timerapp.linkb24.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class WebDavConfigTest {
    @Test
    fun isConfigured_requires_url_and_username() {
        assertFalse(WebDavConfig().isConfigured())
        assertFalse(WebDavConfig(url = "https://example.com").isConfigured())
        assertTrue(
            WebDavConfig(
                url = "https://example.com/dav/",
                username = "user",
            ).isConfigured(),
        )
    }

    @Test
    fun remoteUrl_joins_base_and_path() {
        val config = WebDavConfig(
            url = "https://cloud.example.com/remote.php/dav/files/user",
            remotePath = "tasktimer/data.json",
        )
        assertEquals(
            "https://cloud.example.com/remote.php/dav/files/user/tasktimer/data.json",
            config.remoteUrl(),
        )
    }

    @Test
    fun withDeviceId_generates_id_when_missing() {
        val config = WebDavConfig().withDeviceId()
        assertTrue(config.deviceId.isNotBlank())
        assertEquals(config.deviceId, config.withDeviceId().deviceId)
    }

    @Test
    fun validate_allows_disabled_config() {
        assertNull(validateWebDavConfig(WebDavConfig(enabled = false)))
    }

    @Test
    fun validate_requires_fields_when_enabled() {
        assertNotNull(validateWebDavConfig(WebDavConfig(enabled = true)))
        assertEquals(
            "Укажите URL WebDAV",
            validateWebDavConfig(WebDavConfig(enabled = true, username = "user")),
        )
    }

    @Test
    fun normalizeSyncIntervalMinutes_clampsToAndroidMinimum() {
        assertEquals(0, normalizeSyncIntervalMinutes(0))
        assertEquals(15, normalizeSyncIntervalMinutes(5))
        assertEquals(15, normalizeSyncIntervalMinutes(14))
        assertEquals(20, normalizeSyncIntervalMinutes(20))
    }
}

class WebDavConfigRepositoryTest {
    private fun tempDir(): File = createTempDirectory("webdav-config-test-").toFile()

    @Test
    fun load_returns_defaults_when_missing() {
        val repository = WebDavConfigRepository(File(tempDir(), "webdav.json"))
        val loaded = repository.load()
        assertFalse(loaded.enabled)
        assertTrue(loaded.deviceId.isNotBlank())
    }

    @Test
    fun save_and_load_roundtrip() {
        val dir = tempDir()
        val repository = WebDavConfigRepository(File(dir, "webdav.json"))
        val config = WebDavConfig(
            enabled = true,
            url = "https://example.com/dav/",
            username = "alex",
            password = "secret",
            remotePath = "tasktimer/data.json",
            syncOnStartup = false,
        ).withDeviceId()

        repository.save(config)
        val loaded = repository.load()

        assertTrue(loaded.enabled)
        assertEquals("https://example.com/dav/", loaded.url)
        assertEquals("alex", loaded.username)
        assertEquals("secret", loaded.password)
        assertFalse(loaded.syncOnStartup)
        assertEquals(config.deviceId, loaded.deviceId)
    }
}
