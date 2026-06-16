package com.timerapp.linkb24.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timerapp.linkb24.data.AppDataDto
import com.timerapp.linkb24.data.TaskDto
import com.timerapp.linkb24.data.TaskRepository
import com.timerapp.linkb24.data.TaskStatus
import com.timerapp.linkb24.data.formatDuration
import com.timerapp.linkb24.data.isActive
import com.timerapp.linkb24.data.taskDurationSeconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TaskListUiState(
    val tasks: List<TaskDto> = emptyList(),
    val tickMillis: Long = System.currentTimeMillis(),
    val newTaskTitle: String = "",
    val errorMessage: String? = null,
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(application)
    private var appData: AppDataDto = AppDataDto()

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                appData = withContext(Dispatchers.IO) { repository.load() }
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(tasks = visibleTasks(appData), errorMessage = null)
                }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
            while (isActive) {
                delay(1_000)
                if (_uiState.value.tasks.any(::isActive)) {
                    _uiState.update { it.copy(tickMillis = System.currentTimeMillis()) }
                }
            }
        }
    }

    fun onNewTaskTitleChange(value: String) {
        _uiState.update { it.copy(newTaskTitle = value, errorMessage = null) }
    }

    fun addTask() {
        val title = _uiState.value.newTaskTitle
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val updated = repository.createTask(title, appData)
                    repository.save(updated)
                    updated
                }
            }.onSuccess { updated ->
                appData = updated
                _uiState.update {
                    it.copy(newTaskTitle = "", errorMessage = null, tasks = visibleTasks(appData))
                }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun toggleTimer(taskId: String) {
        mutateTasks("Не удалось изменить таймер") {
            repository.toggleTimer(taskId, appData)
        }
    }

    fun completeTask(taskId: String) {
        mutateTasks("Не удалось завершить задачу") {
            repository.completeTask(taskId, appData)
        }
    }

    fun deleteTask(taskId: String) {
        mutateTasks("Не удалось удалить задачу") {
            repository.deleteTask(taskId, appData)
        }
    }

    fun durationLabel(task: TaskDto): String {
        return formatDuration(taskDurationSeconds(task, _uiState.value.tickMillis))
    }

    private fun mutateTasks(errorPrefix: String, transform: (AppDataDto) -> AppDataDto) {
        viewModelScope.launch {
            val previous = appData
            runCatching {
                withContext(Dispatchers.IO) {
                    val updated = transform(previous)
                    repository.save(updated)
                    updated
                }
            }.onSuccess { updated ->
                appData = updated
                _uiState.update {
                    it.copy(
                        tasks = visibleTasks(appData),
                        tickMillis = System.currentTimeMillis(),
                        errorMessage = null,
                    )
                }
            }.onFailure { error ->
                appData = previous
                _uiState.update {
                    it.copy(errorMessage = "$errorPrefix: ${error.message ?: error.javaClass.simpleName}")
                }
            }
        }
    }

    private fun visibleTasks(data: AppDataDto): List<TaskDto> {
        return data.tasks
            .filter { it.status != TaskStatus.COMPLETED }
            .sortedByDescending { it.createdAt }
    }
}
