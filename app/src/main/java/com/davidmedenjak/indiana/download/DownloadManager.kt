package com.davidmedenjak.indiana.download

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.core.net.toUri
import com.davidmedenjak.indiana.api.BuildArtifactApi
import com.davidmedenjak.indiana.model.V0ArtifactListElementResponseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import android.app.DownloadManager as SystemDownloadManager

@Singleton
class DownloadManager @Inject constructor(
    private val context: Application,
    private val downloadRepository: DownloadRepository,
    private val progressTracker: DownloadProgressTracker,
    private val artifactApi: BuildArtifactApi,
    private val fileOpener: FileOpener,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val systemDownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as SystemDownloadManager
    private val downloadQueue = DownloadQueue()
    
    init {
        // Initialize download recovery on app start
        scope.launch {
            recoverActiveDownloads()
        }
    }

    fun startDownload(
        artifact: V0ArtifactListElementResponseModel,
        appSlug: String,
        buildSlug: String,
        projectId: String,
    ): String {
        val downloadId = UUID.randomUUID().toString()
        
        scope.launch {
            try {
                val details = artifactApi.artifactShow(
                    appSlug = appSlug,
                    buildSlug = buildSlug,
                    artifactSlug = artifact.slug ?: return@launch,
                    download = null,
                ).data ?: return@launch

                val downloadUrl = details.expiringDownloadUrl ?: return@launch
                val fileName = artifact.title ?: "unknown_file"
                val fileSize : Long = details.fileSizeBytes?.toLong() ?: 0L

                // Clean up any previous downloads for this artifact (except the one we're about to create)
                cleanupPreviousDownloadsForArtifact(artifact.slug!!, keepDownloadId = downloadId)

                val downloadState = downloadRepository.createDownload(
                    id = downloadId,
                    artifactId = artifact.slug!!,
                    buildId = buildSlug,
                    projectId = projectId,
                    fileName = fileName,
                    fileSize = fileSize,
                    downloadUrl = downloadUrl,
                )

                downloadQueue.enqueue(downloadState)
                processQueue()

            } catch (e: Exception) {
                downloadRepository.markFailed(downloadId, "Failed to start download: ${e.message}")
            }
        }

        return downloadId
    }

    fun getDownloadById(downloadId: String): Flow<DownloadState?> {
        return downloadRepository.getDownloadById(downloadId)
    }

    fun getDownloadByArtifactId(artifactId: String): Flow<DownloadState?> {
        return downloadRepository.getDownloadByArtifactId(artifactId)
    }

    fun getActiveDownloads(): Flow<List<DownloadState>> {
        return downloadRepository.getActiveDownloads()
    }

    fun getAllDownloads(): Flow<List<DownloadState>> {
        return downloadRepository.getAllDownloads()
    }

    fun getDownloadsByProject(projectId: String): Flow<List<DownloadState>> {
        return downloadRepository.getDownloadsByProject(projectId)
    }

    fun getDownloadsByBuild(buildId: String): Flow<List<DownloadState>> {
        return downloadRepository.getDownloadsByBuild(buildId)
    }

    fun cancelDownload(downloadId: String) {
        scope.launch {
            progressTracker.stopTracking(downloadId)
            downloadRepository.markFailed(downloadId, "Download cancelled by user")
        }
    }

    fun retryDownload(downloadId: String) {
        scope.launch {
            val download = downloadRepository.getDownloadById(downloadId)
            // Convert back to pending state for retry
            // This is a simplified retry - in a real implementation you might want to recreate the download
            downloadRepository.markInProgress(downloadId)
            processQueue()
        }
    }

    suspend fun handleArtifactClick(
        artifact: V0ArtifactListElementResponseModel,
        appSlug: String,
        buildSlug: String,
        projectId: String,
    ): ArtifactClickResult {
        val artifactId = artifact.slug ?: return ArtifactClickResult.Error("Artifact has no ID")
        
        // First, check if we have an existing download for this artifact (gets most recent)
        val currentDownload = downloadRepository.getDownloadByArtifactId(artifactId).first()
        
        return when (currentDownload) {
            is DownloadState.Completed -> {
                // File is downloaded, try to open it
                val success = fileOpener.openFile(
                    context = context,
                    localPath = currentDownload.localPath,
                    fileName = currentDownload.fileName
                )
                if (success) {
                    ArtifactClickResult.FileOpened
                } else {
                    ArtifactClickResult.Error("Failed to open file")
                }
            }
            is DownloadState.Failed -> {
                // Previous download failed, clean up and start a new download
                cleanupPreviousDownloadsForArtifact(artifactId)
                val downloadId = startDownload(artifact, appSlug, buildSlug, projectId)
                ArtifactClickResult.DownloadStarted(downloadId)
            }
            is DownloadState.Pending, is DownloadState.InProgress -> {
                // Download is already in progress, don't start a new one
                ArtifactClickResult.DownloadInProgress
            }
            null -> {
                // No existing download, clean up any old entries and start a new download
                cleanupPreviousDownloadsForArtifact(artifactId)
                val downloadId = startDownload(artifact, appSlug, buildSlug, projectId)
                ArtifactClickResult.DownloadStarted(downloadId)
            }
        }
    }

    private fun processQueue() {
        scope.launch {
            val nextDownload = downloadQueue.dequeue() ?: return@launch
            executeDownload(nextDownload)
        }
    }

    private suspend fun executeDownload(download: DownloadState.Pending) {
        try {
            val downloadUrl = download.downloadUrl.toUri()
            
            val request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && 
                !context.packageManager.canRequestPackageInstalls()) {
                // Simple download without auto-install for devices without permission
                SystemDownloadManager.Request(downloadUrl)
                    .setNotificationVisibility(SystemDownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setTitle(download.fileName)
                    .setDescription("Downloading ${download.fileName}")
            } else {
                // Full download with progress tracking and auto-install
                SystemDownloadManager.Request(downloadUrl)
                    .setNotificationVisibility(SystemDownloadManager.Request.VISIBILITY_VISIBLE)
                    .setTitle(download.fileName)
                    .setDescription("Downloading ${download.fileName}")
            }

            val systemDownloadId = systemDownloadManager.enqueue(request)
            
            // Store the system download ID for recovery
            downloadRepository.updateSystemDownloadId(download.id, systemDownloadId)
            
            // Start progress tracking
            progressTracker.startTracking(download.id, systemDownloadId)
            
        } catch (e: Exception) {
            downloadRepository.markFailed(download.id, "Failed to execute download: ${e.message}")
        }
    }

    private suspend fun recoverActiveDownloads() {
        try {
            val activeDownloads = downloadRepository.getActiveDownloadsWithSystemId()
            
            activeDownloads.forEach { download ->
                // Get the system download ID from the repository
                val downloadEntity = downloadRepository.getDownloadEntity(download.id)
                val systemDownloadId = downloadEntity?.systemDownloadId
                
                if (systemDownloadId != null) {
                    // Check if the system download still exists
                    val query = SystemDownloadManager.Query().setFilterById(systemDownloadId)
                    val cursor = systemDownloadManager.query(query)
                    
                    if (cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(SystemDownloadManager.COLUMN_STATUS)
                        val status = cursor.getInt(statusIndex)
                        
                        when (status) {
                            SystemDownloadManager.STATUS_PENDING,
                            SystemDownloadManager.STATUS_RUNNING -> {
                                // Download is still active, mark as in progress and resume tracking
                                downloadRepository.markInProgress(download.id)
                                progressTracker.startTracking(download.id, systemDownloadId)
                            }
                            SystemDownloadManager.STATUS_SUCCESSFUL -> {
                                // Download completed while app was closed
                                val localUriIndex = cursor.getColumnIndex(SystemDownloadManager.COLUMN_LOCAL_URI)
                                val localUri = cursor.getString(localUriIndex)
                                downloadRepository.markCompleted(download.id, localUri ?: "")
                            }
                            SystemDownloadManager.STATUS_FAILED -> {
                                // Download failed while app was closed
                                val reasonIndex = cursor.getColumnIndex(SystemDownloadManager.COLUMN_REASON)
                                val reason = cursor.getInt(reasonIndex)
                                val errorMessage = getErrorMessage(reason)
                                downloadRepository.markFailed(download.id, errorMessage)
                            }
                        }
                    } else {
                        // System download no longer exists, mark as failed
                        downloadRepository.markFailed(download.id, "Download no longer exists in system")
                    }
                    cursor.close()
                }
            }
        } catch (e: Exception) {
            // Log error but don't crash the app
            android.util.Log.e("DownloadManager", "Error recovering downloads", e)
        }
    }
    
    private fun getErrorMessage(reason: Int): String {
        return when (reason) {
            SystemDownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume download"
            SystemDownloadManager.ERROR_DEVICE_NOT_FOUND -> "Device not found"
            SystemDownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
            SystemDownloadManager.ERROR_FILE_ERROR -> "File error"
            SystemDownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data error"
            SystemDownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient space"
            SystemDownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
            SystemDownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP code"
            SystemDownloadManager.ERROR_UNKNOWN -> "Unknown error"
            else -> "Download failed (code: $reason)"
        }
    }

    private suspend fun cleanupPreviousDownloadsForArtifact(artifactId: String, keepDownloadId: String? = null) {
        val allDownloads = downloadRepository.getAllDownloadsForArtifact(artifactId)
        
        allDownloads.forEach { download ->
            if (download.id != keepDownloadId) {
                // Stop progress tracking if active
                progressTracker.stopTracking(download.id)
                
                // Cancel system download if it's still active
                if (download is DownloadState.Pending || download is DownloadState.InProgress) {
                    val downloadEntity = downloadRepository.getDownloadEntity(download.id)
                    downloadEntity?.systemDownloadId?.let { systemId ->
                        try {
                            systemDownloadManager.remove(systemId)
                        } catch (e: Exception) {
                            // Ignore errors when canceling system downloads
                        }
                    }
                }
            }
        }
        
        // Use repository cleanup for file deletion and database removal
        downloadRepository.cleanupPreviousDownloads(artifactId, keepDownloadId)
    }

    fun cleanup() {
        progressTracker.cleanup()
        scope.cancel()
    }
}