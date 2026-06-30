package com.timerapp.linkb24.data

import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val zoneId: ZoneId = ZoneId.systemDefault()
private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

fun formatTaskDateTime(value: String?): String? {
    if (value.isNullOrBlank()) {
        return null
    }
    val instant = parseInstant(value) ?: return null
    return instant.atZone(zoneId).format(displayFormatter)
}
