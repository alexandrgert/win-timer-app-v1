package com.timerapp.linkb24.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskFiltersTest {
    @Test
    fun today_filter_uses_planned_days() {
        val data = AppDataDto(
            tasks = listOf(
                task("a", plannedDays = listOf("2026-06-28")),
                task("b", plannedDays = listOf("2026-06-27")),
            ),
        )

        val filtered = filterTasks(data, TaskViewFilter.TODAY, today = "2026-06-28")

        assertEquals(listOf("a"), filtered.map { it.id })
    }

    @Test
    fun in_progress_excludes_completed() {
        val data = AppDataDto(
            tasks = listOf(
                task("open"),
                task("done", status = TaskStatus.COMPLETED),
            ),
        )

        val filtered = filterTasks(data, TaskViewFilter.IN_PROGRESS)

        assertEquals(listOf("open"), filtered.map { it.id })
    }

    @Test
    fun all_includes_completed_at_bottom() {
        val data = AppDataDto(
            tasks = listOf(
                task("done", status = TaskStatus.COMPLETED, createdAt = "2026-06-28T12:00:00+03:00"),
                task("open", createdAt = "2026-06-28T11:00:00+03:00"),
            ),
        )

        val filtered = filterTasks(data, TaskViewFilter.ALL)

        assertEquals(listOf("open", "done"), filtered.map { it.id })
    }

    @Test
    fun active_task_moves_to_top() {
        val running = task(
            id = "running",
            status = TaskStatus.RUNNING,
            createdAt = "2026-06-28T09:00:00+03:00",
            sessions = listOf(
                SessionDto(id = "s1", startedAt = "2026-06-28T10:00:00+03:00"),
            ),
        )
        val data = AppDataDto(
            tasks = listOf(
                task("newer", createdAt = "2026-06-28T11:00:00+03:00"),
                running,
            ),
        )

        val filtered = filterTasks(data, TaskViewFilter.IN_PROGRESS)

        assertEquals("running", filtered.first().id)
    }

    private fun task(
        id: String,
        status: TaskStatus = TaskStatus.OPEN,
        createdAt: String = "2026-06-28T10:00:00+03:00",
        plannedDays: List<String> = listOf("2026-06-28"),
        sessions: List<SessionDto> = emptyList(),
    ): TaskDto {
        return TaskDto(
            id = id,
            day = "2026-06-28",
            title = id,
            status = status,
            createdAt = createdAt,
            plannedDays = plannedDays,
            sessions = sessions,
        )
    }
}
