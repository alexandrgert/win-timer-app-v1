package com.timerapp.linkb24.data

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun normalizeRunningTasks(data: AppDataDto): AppDataDto {
    val running = data.tasks.filter { task ->
        task.status == TaskStatus.RUNNING && task.sessions.any { it.endedAt == null }
    }
    if (running.size <= 1) {
        return data
    }
    val winner = running.maxByOrNull { task -> latestRunningStartMillis(task) } ?: return data
    val now = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    val tasks = data.tasks.map { task ->
        if (task.id == winner.id) {
            return@map task
        }
        if (task.status != TaskStatus.RUNNING && task.sessions.none { it.endedAt == null }) {
            return@map task
        }
        val sessions = task.sessions.map { session ->
            if (session.endedAt == null) session.copy(endedAt = now) else session
        }
        task.copy(status = TaskStatus.PAUSED, sessions = sessions)
    }
    return data.copy(tasks = tasks)
}

private fun latestRunningStartMillis(task: TaskDto): Long {
    val startedAt = task.sessions.lastOrNull { it.endedAt == null }?.startedAt ?: return Long.MIN_VALUE
    return parseInstant(startedAt)?.toEpochMilli() ?: Long.MIN_VALUE
}
