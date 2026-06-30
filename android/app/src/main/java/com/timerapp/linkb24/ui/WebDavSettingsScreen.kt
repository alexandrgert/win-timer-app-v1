package com.timerapp.linkb24.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timerapp.linkb24.BuildConfig
import com.timerapp.linkb24.R
import com.timerapp.linkb24.webdav.REMIND_LATER_MINUTES_CHOICES

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebDavSettingsScreen(
    onBack: () -> Unit,
    onSyncComplete: () -> Unit = {},
    viewModel: WebDavSettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.webdav_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.webdav_settings_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SettingsCheckboxRow(
                label = stringResource(R.string.webdav_enabled),
                checked = uiState.enabled,
                onCheckedChange = viewModel::onEnabledChange,
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.url,
                onValueChange = viewModel::onUrlChange,
                label = { Text(stringResource(R.string.webdav_url)) },
                placeholder = { Text(stringResource(R.string.webdav_url_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.username,
                onValueChange = viewModel::onUsernameChange,
                label = { Text(stringResource(R.string.webdav_username)) },
                singleLine = true,
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(stringResource(R.string.webdav_password)) },
                singleLine = true,
                visualTransformation = if (uiState.showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = viewModel::toggleShowPassword) {
                        Icon(
                            imageVector = if (uiState.showPassword) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = stringResource(
                                if (uiState.showPassword) {
                                    R.string.webdav_hide_password
                                } else {
                                    R.string.webdav_show_password
                                },
                            ),
                        )
                    }
                },
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.remotePath,
                onValueChange = viewModel::onRemotePathChange,
                label = { Text(stringResource(R.string.webdav_remote_path)) },
                placeholder = { Text(stringResource(R.string.webdav_remote_path_placeholder)) },
                singleLine = true,
            )

            SettingsCheckboxRow(
                label = stringResource(R.string.webdav_sync_on_startup),
                checked = uiState.syncOnStartup,
                onCheckedChange = viewModel::onSyncOnStartupChange,
            )

            SettingsCheckboxRow(
                label = stringResource(R.string.webdav_sync_on_shutdown),
                checked = uiState.syncOnShutdown,
                onCheckedChange = viewModel::onSyncOnShutdownChange,
            )

            SettingsCheckboxRow(
                label = stringResource(R.string.webdav_shutdown_upload_only),
                checked = uiState.shutdownUploadOnly,
                onCheckedChange = viewModel::onShutdownUploadOnlyChange,
            )

            Text(
                text = stringResource(R.string.webdav_upload_only_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.syncIntervalMinutes,
                onValueChange = viewModel::onSyncIntervalMinutesChange,
                label = { Text(stringResource(R.string.webdav_sync_interval_minutes)) },
                placeholder = { Text(stringResource(R.string.webdav_sync_interval_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            Text(
                text = stringResource(R.string.webdav_sync_interval_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(R.string.webdav_remind_later_minutes),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                REMIND_LATER_MINUTES_CHOICES.forEach { minutes ->
                    FilterChip(
                        selected = uiState.syncRemindLaterMinutes == minutes,
                        onClick = { viewModel.onSyncRemindLaterMinutesChange(minutes) },
                        label = { Text("$minutes") },
                    )
                }
            }
            Text(
                text = stringResource(R.string.webdav_remind_later_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = viewModel::testConnection,
                    enabled = !uiState.isTesting && !uiState.isSaving && !uiState.isSyncing,
                ) {
                    Text(
                        if (uiState.isTesting) {
                            stringResource(R.string.webdav_testing)
                        } else {
                            stringResource(R.string.webdav_test)
                        },
                    )
                }
                FilledTonalButton(
                    onClick = { viewModel.save() },
                    enabled = !uiState.isTesting && !uiState.isSaving && !uiState.isSyncing,
                ) {
                    Text(
                        if (uiState.isSaving) {
                            stringResource(R.string.webdav_saving)
                        } else {
                            stringResource(R.string.webdav_save)
                        },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = { viewModel.pullNow(onSyncComplete) },
                    enabled = !uiState.isTesting && !uiState.isSaving && !uiState.isSyncing,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        if (uiState.isSyncing) {
                            stringResource(R.string.webdav_syncing)
                        } else {
                            stringResource(R.string.webdav_pull)
                        },
                    )
                }
                FilledTonalButton(
                    onClick = { viewModel.pushNow(onSyncComplete) },
                    enabled = !uiState.isTesting && !uiState.isSaving && !uiState.isSyncing,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.webdav_push))
                }
            }

            uiState.statusMessage?.let { message ->
                Text(message, style = MaterialTheme.typography.bodySmall)
            }

            uiState.savedMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.primary)
            }

            uiState.errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error)
            }

            Text(
                text = stringResource(
                    R.string.app_version_format,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.app_update_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
