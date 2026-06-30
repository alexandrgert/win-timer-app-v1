package com.timerapp.linkb24.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timerapp.linkb24.data.TaskRepository
import com.timerapp.linkb24.data.WebDavConfig
import com.timerapp.linkb24.data.WebDavConfigRepository
import com.timerapp.linkb24.data.normalizeSyncIntervalMinutes
import com.timerapp.linkb24.data.validateWebDavConfig
import com.timerapp.linkb24.webdav.WebDavClient
import com.timerapp.linkb24.webdav.WebDavException
import com.timerapp.linkb24.webdav.WebDavSync
import com.timerapp.linkb24.webdav.normalizeRemindLaterMinutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class WebDavSettingsUiState(
    val enabled: Boolean = false,
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val remotePath: String = "",
    val syncOnStartup: Boolean = true,
    val syncOnShutdown: Boolean = true,
    val shutdownUploadOnly: Boolean = false,
    val syncIntervalMinutes: String = "0",
    val syncRemindLaterMinutes: Int = 15,
    val showPassword: Boolean = false,
    val lastSyncAt: String? = null,
    val lastError: String = "",
    val statusMessage: String? = null,
    val isTesting: Boolean = false,
    val isSaving: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val savedMessage: String? = null,
)

class WebDavSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WebDavConfigRepository(application)
    private val webDavSync = WebDavSync(TaskRepository(application), repository)

    private val _uiState = MutableStateFlow(WebDavSettingsUiState())
    val uiState: StateFlow<WebDavSettingsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        val config = repository.load()
        _uiState.value = config.toUiState()
    }

    fun onEnabledChange(value: Boolean) {
        _uiState.update { it.copy(enabled = value, errorMessage = null, savedMessage = null) }
    }

    fun onUrlChange(value: String) {
        _uiState.update { it.copy(url = value, errorMessage = null, savedMessage = null) }
    }

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null, savedMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null, savedMessage = null) }
    }

    fun onRemotePathChange(value: String) {
        _uiState.update { it.copy(remotePath = value, errorMessage = null, savedMessage = null) }
    }

    fun onSyncOnStartupChange(value: Boolean) {
        _uiState.update { it.copy(syncOnStartup = value, savedMessage = null) }
    }

    fun onSyncOnShutdownChange(value: Boolean) {
        _uiState.update { it.copy(syncOnShutdown = value, savedMessage = null) }
    }

    fun onShutdownUploadOnlyChange(value: Boolean) {
        _uiState.update { it.copy(shutdownUploadOnly = value, savedMessage = null) }
    }

    fun onSyncIntervalMinutesChange(value: String) {
        _uiState.update { it.copy(syncIntervalMinutes = value.filter { it.isDigit() }, savedMessage = null) }
    }

    fun onSyncRemindLaterMinutesChange(value: Int) {
        _uiState.update { it.copy(syncRemindLaterMinutes = value, savedMessage = null) }
    }

    fun toggleShowPassword() {
        _uiState.update { it.copy(showPassword = !it.showPassword) }
    }

    fun save(): Boolean {
        val config = _uiState.value.toConfig()
        val validationError = validateWebDavConfig(config)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError, savedMessage = null) }
            return false
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, savedMessage = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.save(config.withDeviceId())
                }
            }.onSuccess {
                (getApplication() as com.timerapp.linkb24.TimerApplication).restartWebDavPeriodicMonitor()
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedMessage = "Настройки сохранены",
                        statusMessage = statusText(config),
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Не удалось сохранить настройки",
                    )
                }
            }
        }
        return true
    }

    fun testConnection() {
        val config = _uiState.value.toConfig()
        val validationError = validateWebDavConfig(config.takeIf { it.enabled } ?: config.copy(enabled = true))
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(isTesting = true, errorMessage = null, statusMessage = null, savedMessage = null)
            }
            runCatching {
                withContext(Dispatchers.IO) {
                    WebDavClient(config.copy(enabled = true)).testConnection()
                }
            }.onSuccess { message ->
                _uiState.update {
                    it.copy(isTesting = false, statusMessage = message, lastError = "")
                }
            }.onFailure { error ->
                val text = when (error) {
                    is WebDavException -> error.message ?: "Ошибка WebDAV"
                    else -> error.message ?: "Ошибка WebDAV"
                }
                _uiState.update {
                    it.copy(isTesting = false, errorMessage = text, lastError = text)
                }
            }
        }
    }

    fun pullNow(onComplete: () -> Unit = {}) {
        if (!persistFormConfig()) {
            return
        }
        syncAction(onComplete = onComplete) { config ->
            webDavSync.pullAndMerge(config, requireEnabled = false)
        }
    }

    fun pushNow(onComplete: () -> Unit = {}) {
        if (!persistFormConfig()) {
            return
        }
        syncAction(onComplete = onComplete) { config ->
            webDavSync.pushLocal(config, requireEnabled = false)
        }
    }

    private fun persistFormConfig(): Boolean {
        val config = _uiState.value.toConfig()
        val validationError = validateWebDavConfig(config.copy(enabled = true))
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return false
        }
        repository.save(config.withDeviceId())
        return true
    }

    private fun syncAction(
        onComplete: () -> Unit,
        block: suspend (WebDavConfig) -> com.timerapp.linkb24.webdav.SyncOutcome,
    ) {
        val config = repository.load()
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSyncing = true, errorMessage = null, savedMessage = null)
            }
            runCatching {
                withContext(Dispatchers.IO) { block(config) }
            }.onSuccess { outcome ->
                load()
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        statusMessage = outcome.notice.ifBlank {
                            outcome.error.ifBlank { "Синхронизация завершена" }
                        },
                        errorMessage = outcome.error.ifBlank { null },
                    )
                }
                onComplete()
            }.onFailure { error ->
                val text = when (error) {
                    is WebDavException -> error.message ?: "Ошибка WebDAV"
                    else -> error.message ?: "Ошибка WebDAV"
                }
                _uiState.update {
                    it.copy(isSyncing = false, errorMessage = text, lastError = text)
                }
            }
        }
    }

    private fun WebDavConfig.toUiState(): WebDavSettingsUiState {
        return WebDavSettingsUiState(
            enabled = enabled,
            url = url,
            username = username,
            password = password,
            remotePath = remotePath,
            syncOnStartup = syncOnStartup,
            syncOnShutdown = syncOnShutdown,
            shutdownUploadOnly = shutdownUploadOnly,
            syncIntervalMinutes = syncIntervalMinutes.toString(),
            syncRemindLaterMinutes = normalizeRemindLaterMinutes(syncRemindLaterMinutes),
            lastSyncAt = lastSyncAt,
            lastError = lastError,
            statusMessage = statusText(this),
        )
    }

    private fun WebDavSettingsUiState.toConfig(): WebDavConfig {
        val current = repository.load()
        return WebDavConfig(
            enabled = enabled,
            url = url.trim(),
            username = username.trim(),
            password = password,
            remotePath = remotePath.trim().ifEmpty { com.timerapp.linkb24.data.DEFAULT_WEBDAV_REMOTE_PATH },
            syncOnStartup = syncOnStartup,
            syncOnShutdown = syncOnShutdown,
            shutdownUploadOnly = shutdownUploadOnly,
            syncIntervalMinutes = normalizeSyncIntervalMinutes(syncIntervalMinutes.toIntOrNull() ?: 0),
            syncRemindLaterMinutes = normalizeRemindLaterMinutes(syncRemindLaterMinutes),
            lastSyncAt = current.lastSyncAt,
            lastError = current.lastError,
            deviceId = current.deviceId,
            lastRemoteContentHash = current.lastRemoteContentHash,
            lastSyncHadConflict = current.lastSyncHadConflict,
            pendingNotice = current.pendingNotice,
            pendingRemoteHash = current.pendingRemoteHash,
            pendingRemoteRemindAt = current.pendingRemoteRemindAt,
        ).withDeviceId()
    }

    private fun statusText(config: WebDavConfig): String {
        val parts = mutableListOf<String>()
        if (config.lastSyncAt != null) {
            parts += "Последняя синхронизация: ${config.lastSyncAt}"
        }
        if (config.lastError.isNotBlank()) {
            parts += "Ошибка: ${config.lastError}"
        }
        if (parts.isEmpty()) {
            return if (config.enabled) "Синхронизация включена" else "Синхронизация выключена"
        }
        return parts.joinToString("\n")
    }
}
