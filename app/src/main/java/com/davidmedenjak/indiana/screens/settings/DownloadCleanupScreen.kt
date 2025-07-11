package com.davidmedenjak.indiana.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.settings.CleanupPeriod
import com.davidmedenjak.indiana.settings.DownloadCleanupSettings
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.atoms.Button
import com.davidmedenjak.indiana.theme.ui.atoms.Card
import com.davidmedenjak.indiana.theme.ui.atoms.ExposedDropdownTextField
import com.davidmedenjak.indiana.theme.ui.atoms.Icon
import com.davidmedenjak.indiana.theme.ui.atoms.IndeterminateProgressCircular
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.molectule.Confirmation
import com.davidmedenjak.indiana.theme.ui.molectule.PropertyLayout
import com.davidmedenjak.indiana.theme.ui.molectule.rememberConfirmationDialogState
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun CleanupPeriod.getLocalizedDisplayName(context: android.content.Context): String {
    return when (this) {
        CleanupPeriod.ONE_DAY -> context.getString(R.string.cleanup_period_one_day)
        CleanupPeriod.THREE_DAYS -> context.getString(R.string.cleanup_period_three_days)
        CleanupPeriod.SEVEN_DAYS -> context.getString(R.string.cleanup_period_seven_days)
        CleanupPeriod.THIRTY_DAYS -> context.getString(R.string.cleanup_period_thirty_days)
        CleanupPeriod.NEVER -> context.getString(R.string.cleanup_period_never)
    }
}

@Composable
fun DownloadCleanupScreen(
    uiState: DownloadCleanupUiState,
    onCleanupPeriodChanged: (CleanupPeriod) -> Unit,
    onRunCleanupNow: () -> Unit,
    onClearAllDownloads: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val clearAllDialog = rememberConfirmationDialogState()

    // Show error messages as toast or snackbar would be handled elsewhere
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // In a real app, this would show a snackbar
            // For now, we'll just dismiss the error after showing it
            coroutineScope.launch {
                onDismissError()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Storage Usage Section
        StorageUsageCard(
            storageUsage = uiState.storageUsage,
            isLoading = uiState.isLoading
        )

        val context = LocalContext.current
        DownloadManagementCard(
            selectedPeriod = uiState.selectedCleanupPeriod,
            onPeriodChanged = onCleanupPeriodChanged,
            lastCleanupTime = uiState.cleanupSettings.lastCleanupTime,
            isRunningCleanup = uiState.isRunningCleanup,
            onRunCleanupNow = onRunCleanupNow,
            isClearingAll = uiState.isClearingAll,
            onClearAllDownloads = {
                clearAllDialog.confirm(
                    Confirmation(
                        title = context.getString(R.string.download_cleanup_confirmation_clear_all_title),
                        text = context.getString(R.string.download_cleanup_confirmation_clear_all_text),
                        action = context.getString(R.string.download_cleanup_confirmation_clear_all_action),
                        callback = { onClearAllDownloads() },
                    )
                )
            },
            hasDownloads = uiState.storageUsage > 0
        )
    }
}

@Composable
private fun StorageUsageCard(
    storageUsage: Long,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = IndianaTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.download_cleanup_storage_usage_title),
                    style = IndianaTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IndeterminateProgressCircular()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.download_cleanup_storage_calculating))
                }
            } else {
                Text(
                    text = formatFileSize(storageUsage),
                    style = IndianaTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = IndianaTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.download_cleanup_storage_total_size),
                    style = IndianaTheme.typography.bodyMedium,
                    color = IndianaTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DownloadManagementCard(
    selectedPeriod: CleanupPeriod,
    onPeriodChanged: (CleanupPeriod) -> Unit,
    lastCleanupTime: Instant?,
    isRunningCleanup: Boolean,
    onRunCleanupNow: () -> Unit,
    isClearingAll: Boolean,
    onClearAllDownloads: () -> Unit,
    hasDownloads: Boolean
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.download_cleanup_management_title),
                style = IndianaTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownTextField(
                value = selectedPeriod,
                onValueChange = onPeriodChanged,
                options = CleanupPeriod.entries,
                label = { Text(stringResource(R.string.download_cleanup_auto_delete_label)) },
                optionText = { it.getLocalizedDisplayName(context) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PropertyLayout(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)) {
                Text(stringResource(R.string.download_cleanup_last_cleanup))
                Text(
                    text = lastCleanupTime?.let { formatDateTime(it) }
                        ?: stringResource(R.string.download_cleanup_never),
                    color = IndianaTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRunCleanupNow,
                    enabled = !isRunningCleanup && !isClearingAll && selectedPeriod != CleanupPeriod.NEVER,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isRunningCleanup) {
                        IndeterminateProgressCircular(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.download_cleanup_button_cleanup))
                    }
                }

                Button(
                    onClick = onClearAllDownloads,
                    enabled = !isClearingAll && !isRunningCleanup && hasDownloads,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isClearingAll) {
                        IndeterminateProgressCircular()
                    } else {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.download_cleanup_button_clear_all))
                    }
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes == 0L) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0

    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }

    return String.format(Locale.getDefault(), "%.1f %s", size, units[unitIndex])
}

private fun formatDateTime(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

@PreviewLightDark
@Composable
private fun DownloadCleanupScreenPreview() {
    PreviewSurface {
        DownloadCleanupScreen(
            uiState = DownloadCleanupUiState(
                cleanupSettings = DownloadCleanupSettings(
                    cleanupPeriod = CleanupPeriod.THREE_DAYS,
                    lastCleanupTime = Instant.now(),
                    isEnabled = true
                ),
                selectedCleanupPeriod = CleanupPeriod.THREE_DAYS,
                storageUsage = 1024 * 1024 * 150, // 150 MB
                isLoading = false
            ),
            onCleanupPeriodChanged = {},
            onRunCleanupNow = {},
            onClearAllDownloads = {},
            onDismissError = {}
        )
    }
}