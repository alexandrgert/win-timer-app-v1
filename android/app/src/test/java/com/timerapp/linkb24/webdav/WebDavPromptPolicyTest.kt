package com.timerapp.linkb24.webdav

import com.timerapp.linkb24.data.WebDavConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class WebDavPromptPolicyTest {
    @Test
    fun shouldShow_whenNoPending() {
        val config = WebDavConfig()
        assertTrue(shouldShowRemotePrompt(config, "hash-a"))
    }

    @Test
    fun shouldShow_whenNewHash() {
        val config = WebDavConfig(
            pendingRemoteHash = "hash-a",
            pendingRemoteRemindAt = futureIso(),
        )
        assertTrue(shouldShowRemotePrompt(config, "hash-b"))
    }

    @Test
    fun shouldNotShow_beforeRemindAt() {
        val config = WebDavConfig(
            pendingRemoteHash = "hash-a",
            pendingRemoteRemindAt = futureIso(),
        )
        assertFalse(shouldShowRemotePrompt(config, "hash-a"))
    }

    @Test
    fun shouldShow_afterRemindAt() {
        val config = WebDavConfig(
            pendingRemoteHash = "hash-a",
            pendingRemoteRemindAt = pastIso(),
        )
        assertTrue(shouldShowRemotePrompt(config, "hash-a"))
    }

    @Test
    fun prepareRemotePrompt_clearsPendingForNewHash() {
        val config = WebDavConfig(
            pendingRemoteHash = "hash-a",
            pendingRemoteRemindAt = futureIso(),
        )
        val prepared = config.prepareRemotePrompt("hash-b")
        assertTrue(prepared.pendingRemoteHash.isEmpty())
        assertTrue(prepared.pendingRemoteRemindAt == null)
    }

    private fun futureIso(): String {
        return OffsetDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    private fun pastIso(): String {
        return OffsetDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}
