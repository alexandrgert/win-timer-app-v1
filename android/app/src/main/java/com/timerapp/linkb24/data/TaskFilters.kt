package com.timerapp.linkb24.data

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class TaskViewFilter {
    TODAY,
    IN_PROGRESS,
    ALL,
}

fun todayIsoDate(): String {
    return LocalDate.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE)
}

fun filterTasks(
    data: AppDataDto,
    filter: TaskViewFilter,
    today: String = todayIsoDate(),
): List<TaskDto> {
    val filtered = when (filter) {
        TaskViewFilter.TODAY -> data.tasks.filter { today in it.plannedDays }
        TaskViewFilter.IN_PROGRESS -> data.tasks.filter { it.status != TaskStatus.COMPLETED }
        TaskViewFilter.ALL -> data.tasks
    }
    return sortTasksForView(filtered, data.tasks)
}

fun sortTasksForView(tasks: List<TaskDto>, allTasks: List<TaskDto>): List<TaskDto> {
    var ordered = tasks.sortedByDescending { it.createdAt }
    val activeTask = allTasks.firstOrNull(::isActive)
    if (activeTask != null && ordered.any { it.id == activeTask.id }) {
        ordered = listOf(activeTask) + ordered.filter { it.id != activeTask.id }
    }
    return ordered.sortedBy { it.status == TaskStatus.COMPLETED }
}
