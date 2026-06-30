package com.timerapp.linkb24.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskDateTimeFormatTest {
    @Test
    fun formatTaskDateTime_parsesOffsetTimestamp() {
        val formatted = formatTaskDateTime("2026-06-28T20:31:00+03:00")
        assertEquals("28.06.2026 20:31", formatted)
    }

    @Test
    fun formatTaskDateTime_returnsNullForBlank() {
        assertNull(formatTaskDateTime(""))
        assertNull(formatTaskDateTime(null))
    }
}
