package com.davidmedenjak.indiana.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.davidmedenjak.indiana.app.UserSettings
import com.davidmedenjak.indiana.download.DownloadRepository
import com.davidmedenjak.indiana.download.DownloadState
import com.davidmedenjak.indiana.settings.CleanupPeriod
import com.davidmedenjak.indiana.settings.DownloadCleanupSettings
import com.davidmedenjak.indiana.work.FileCleanupWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DownloadCleanupViewModel @Inject constructor(
    private val userSettings: UserSettings,
    private val downloadRepository: DownloadRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadCleanupUiState())
    val uiState: StateFlow<DownloadCleanupUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        observeStorageUsage()
    }

    private fun loadSettings() {
        val settings = userSettings.downloadCleanupSettings
        _uiState.value = _uiState.value.copy(
            cleanupSettings = settings,
            selectedCleanupPeriod = settings.cleanupPeriod
        )
    }

    private fun observeStorageUsage() {
        viewModelScope.launch {
            try {
                downloadRepository.getStorageUsageFlow().collect { storageUsage ->
                    _uiState.value = _uiState.value.copy(
                        storageUsage = storageUsage,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to calculate storage usage: ${e.message}"
                )
            }
        }
    }

    fun updateCleanupPeriod(period: CleanupPeriod) {
        val currentSettings = _uiState.value.cleanupSettings
        val updatedSettings = currentSettings.copy(cleanupPeriod = period)
        
        userSettings.downloadCleanupSettings = updatedSettings
        _uiState.value = _uiState.value.copy(
            cleanupSettings = updatedSettings,
            selectedCleanupPeriod = period
        )
    }

    fun runCleanupNow() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isRunningCleanup = true)
                
                val cleanupPeriod = _uiState.value.cleanupSettings.cleanupPeriod
                if (cleanupPeriod != CleanupPeriod.NEVER) {
                    downloadRepository.cleanupOldDownloads(cleanupPeriod.days)
                }
                
                // Update last cleanup time
                val updatedSettings = _uiState.value.cleanupSettings.copy(
                    lastCleanupTime = Instant.now()
                )
                userSettings.downloadCleanupSettings = updatedSettings
                
                _uiState.value = _uiState.value.copy(
                    cleanupSettings = updatedSettings,
                    isRunningCleanup = false
                )
                
                // Storage usage will be automatically updated via the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRunningCleanup = false,
                    error = "Cleanup failed: ${e.message}"
                )
            }
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isClearingAll = true)
                
                downloadRepository.clearAllDownloads()
                
                _uiState.value = _uiState.value.copy(
                    isClearingAll = false,
                    storageUsage = 0L
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isClearingAll = false,
                    error = "Failed to clear all downloads: ${e.message}"
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Debug method to check download entries
    fun debugDownloads() {
        viewModelScope.launch {
            try {
                val allDownloads = downloadRepository.getAllDownloads().first()
                val completedDownloads = allDownloads.filter { it is DownloadState.Completed }
                
                val debugInfo = buildString {
                    appendLine("Total downloads: ${allDownloads.size}")
                    appendLine("Completed downloads: ${completedDownloads.size}")
                    completedDownloads.forEach { download ->
                        if (download is DownloadState.Completed) {
                            appendLine("- ${download.fileName}: ${download.localPath}")
                        }
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    error = "Debug info:\n$debugInfo"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Debug failed: ${e.message}"
                )
            }
        }
    }
}

data class DownloadCleanupUiState(
    val cleanupSettings: DownloadCleanupSettings = DownloadCleanupSettings(),
    val selectedCleanupPeriod: CleanupPeriod = CleanupPeriod.THREE_DAYS,
    val storageUsage: Long = 0L,
    val isLoading: Boolean = true,
    val isRunningCleanup: Boolean = false,
    val isClearingAll: Boolean = false,
    val error: String? = null
)