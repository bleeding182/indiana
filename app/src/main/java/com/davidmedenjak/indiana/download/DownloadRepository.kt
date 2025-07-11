package com.davidmedenjak.indiana.download

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import com.davidmedenjak.indiana.db.AppDatabase
import com.davidmedenjak.indiana.db.DownloadDao
import com.davidmedenjak.indiana.db.DownloadEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    private val context: Application,
    private val database: AppDatabase,
) {
    private val downloadDao: DownloadDao = database.downloads()

    suspend fun createDownload(
        id: String,
        artifactId: String,
        buildId: String,
        projectId: String,
        fileName: String,
        fileSize: Long,
        downloadUrl: String,
    ): DownloadState.Pending {
        val download = DownloadEntity(
            id = id,
            artifactId = artifactId,
            buildId = buildId,
            projectId = projectId,
            fileName = fileName,
            fileSize = fileSize,
            downloadedBytes = 0L,
            downloadUrl = downloadUrl,
            localPath = null,
            state = DownloadState.STATE_PENDING,
            systemDownloadId = null,
            createdAt = Instant.now(),
            completedAt = null,
            errorMessage = null
        )
        downloadDao.insert(download)
        return download.toDownloadState() as DownloadState.Pending
    }

    suspend fun updateProgress(id: String, downloadedBytes: Long) {
        downloadDao.updateProgress(id, downloadedBytes)
    }

    suspend fun markCompleted(id: String, localPath: String) {
        downloadDao.updateCompleted(
            id = id,
            state = DownloadState.STATE_COMPLETED,
            completedAt = Instant.now(),
            localPath = localPath
        )
    }

    suspend fun markFailed(id: String, errorMessage: String) {
        downloadDao.updateFailed(
            id = id,
            state = DownloadState.STATE_FAILED,
            errorMessage = errorMessage
        )
    }

    suspend fun markInProgress(id: String) {
        val download = downloadDao.getById(id)
        if (download != null && download.state == DownloadState.STATE_PENDING) {
            val updated = download.copy(
                state = DownloadState.STATE_IN_PROGRESS,
                downloadedBytes = 0L // Reset progress when transitioning to in progress
            )
            downloadDao.update(updated)
        }
    }

    suspend fun deleteDownload(id: String) {
        val download = downloadDao.getById(id)
        if (download != null) {
            downloadDao.delete(download)
        }
    }

    fun getDownloadById(id: String): Flow<DownloadState?> {
        return downloadDao.getByIdFlow(id).map { entity ->
            entity?.toDownloadState()
        }
    }

    fun getDownloadByArtifactId(artifactId: String): Flow<DownloadState?> {
        return downloadDao.getByArtifactIdFlow(artifactId).map { entity ->
            entity?.toDownloadState()
        }
    }

    fun getActiveDownloads(): Flow<List<DownloadState>> {
        return downloadDao.getActiveDownloads().map { entities ->
            entities.map { it.toDownloadState() }
        }
    }

    fun getAllDownloads(): Flow<List<DownloadState>> {
        return downloadDao.getAllDownloads().map { entities ->
            entities.map { it.toDownloadState() }
        }
    }

    fun getDownloadsByProject(projectId: String): Flow<List<DownloadState>> {
        return downloadDao.getDownloadsByProject(projectId).map { entities ->
            entities.map { it.toDownloadState() }
        }
    }

    fun getDownloadsByBuild(buildId: String): Flow<List<DownloadState>> {
        return downloadDao.getDownloadsByBuild(buildId).map { entities ->
            entities.map { it.toDownloadState() }
        }
    }

    suspend fun updateSystemDownloadId(id: String, systemDownloadId: Long) {
        downloadDao.updateSystemDownloadId(id, systemDownloadId)
    }

    suspend fun getBySystemDownloadId(systemDownloadId: Long): DownloadState? {
        return downloadDao.getBySystemDownloadId(systemDownloadId)?.toDownloadState()
    }

    suspend fun getActiveDownloadsWithSystemId(): List<DownloadState> {
        return downloadDao.getActiveDownloadsWithSystemId().map { it.toDownloadState() }
    }

    suspend fun getDownloadEntity(id: String): DownloadEntity? {
        return downloadDao.getById(id)
    }

    suspend fun getAllDownloadsForArtifact(artifactId: String): List<DownloadState> {
        return downloadDao.getAllByArtifactId(artifactId).map { it.toDownloadState() }
    }

    suspend fun cleanupPreviousDownloads(artifactId: String, keepDownloadId: String? = null) {
        val allDownloads = downloadDao.getAllByArtifactId(artifactId)
        
        allDownloads.forEach { download ->
            if (download.id != keepDownloadId) {
                // Cancel system download if it's still active
                if (download.systemDownloadId != null && 
                    (download.state == DownloadState.STATE_PENDING || download.state == DownloadState.STATE_IN_PROGRESS)) {
                    try {
                        val downloadManager = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
                        downloadManager.remove(download.systemDownloadId)
                    } catch (e: Exception) {
                        // Ignore errors when canceling system downloads
                    }
                }
                
                // Delete local file if it exists
                download.localPath?.let { path ->
                    deleteFileFromPath(path)
                }
                
                // Remove from database
                downloadDao.delete(download)
            }
        }
    }

    suspend fun cleanupOldDownloads(daysOld: Int = 7) {
        val cutoffTime = Instant.now().minusSeconds(daysOld * 24 * 60 * 60L)

        // Get old downloads with files before deleting DB entries
        val oldCompletedDownloads = downloadDao.getOldCompletedDownloadsWithFiles(cutoffTime)
        val oldFailedDownloads = downloadDao.getOldFailedDownloadsWithFiles(cutoffTime)

        // Delete files first
        (oldCompletedDownloads + oldFailedDownloads).forEach { download ->
            download.localPath?.let { path ->
                deleteFileFromPath(path)
            }
        }

        // Then delete database entries
        downloadDao.deleteOldCompletedDownloads(cutoffTime)
        downloadDao.deleteOldFailedDownloads(cutoffTime)
    }

    suspend fun calculateStorageUsage(): Long {
        val allDownloads = downloadDao.getAllDownloadsList()
        return allDownloads.sumOf { entity ->
            if (entity.state == DownloadState.STATE_COMPLETED && entity.localPath != null) {
                getFileSizeFromPath(entity.localPath)
            } else {
                0L
            }
        }
    }

    fun getStorageUsageFlow(): Flow<Long> {
        return downloadDao.getAllDownloads().map { entities ->
            entities.sumOf { entity ->
                if (entity.state == DownloadState.STATE_COMPLETED && entity.localPath != null) {
                    getFileSizeFromPath(entity.localPath)
                } else {
                    0L
                }
            }
        }
    }

    private fun getFileSizeFromPath(path: String): Long {
        return try {
            if (path.startsWith("content://") || path.startsWith("file://")) {
                // Handle URI paths
                val uri = path.toUri()
                getFileSizeFromUri(uri)
            } else {
                // Handle direct file paths
                val file = java.io.File(path)
                if (file.exists()) file.length() else 0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    private fun getFileSizeFromUri(uri: Uri): Long {
        return try {
            when (uri.scheme) {
                "file" -> {
                    // File URI - extract path and get file size
                    val file = java.io.File(uri.path ?: return 0L)
                    if (file.exists()) file.length() else 0L
                }

                "content" -> {
                    // Content URI - use ContentResolver to get file size
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        // For content URIs, we might need to read the actual file size
                        // This is more complex but necessary for accurate measurement
                        var size = 0L
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            size += bytesRead
                        }
                        size
                    } ?: 0L
                }

                else -> 0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    private fun deleteFileFromPath(path: String) {
        try {
            if (path.startsWith("content://") || path.startsWith("file://")) {
                // Handle URI paths
                val uri = path.toUri()
                deleteFileFromUri(uri)
            } else {
                // Handle direct file paths
                val file = java.io.File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // Log error but continue with cleanup
        }
    }

    private fun deleteFileFromUri(uri: Uri) {
        try {
            when (uri.scheme) {
                "file" -> {
                    // File URI - extract path and delete file
                    val file = java.io.File(uri.path ?: return)
                    if (file.exists()) {
                        file.delete()
                    }
                }

                "content" -> {
                    // Content URI - attempt to delete through DocumentsContract
                    // Note: This might not always work depending on permissions
                    try {
                        context.contentResolver.delete(uri, null, null)
                    } catch (e: Exception) {
                        // Content deletion may fail, that's okay
                    }
                }
            }
        } catch (e: Exception) {
            // Log error but continue with cleanup
        }
    }

    suspend fun clearAllDownloads() {
        // Get all downloads with files
        val allDownloads = downloadDao.getAllDownloadsList()
        val downloadsWithFiles = allDownloads.filter { it.localPath != null }

        // Delete files first
        downloadsWithFiles.forEach { download ->
            download.localPath?.let { path ->
                deleteFileFromPath(path)
            }
        }

        // Delete all database entries
        downloadsWithFiles.forEach { download ->
            downloadDao.delete(download)
        }
    }
}

private fun DownloadEntity.toDownloadState(): DownloadState {
    return when (state) {
        DownloadState.STATE_PENDING -> DownloadState.Pending(
            id = id,
            artifactId = artifactId,
            buildId = buildId,
            projectId = projectId,
            fileName = fileName,
            fileSize = fileSize,
            downloadUrl = downloadUrl,
            createdAt = createdAt,
        )

        DownloadState.STATE_IN_PROGRESS -> DownloadState.InProgress(
            id = id,
            artifactId = artifactId,
            buildId = buildId,
            projectId = projectId,
            fileName = fileName,
            fileSize = fileSize,
            downloadUrl = downloadUrl,
            createdAt = createdAt,
            downloadedBytes = downloadedBytes,
        )

        DownloadState.STATE_COMPLETED -> DownloadState.Completed(
            id = id,
            artifactId = artifactId,
            buildId = buildId,
            projectId = projectId,
            fileName = fileName,
            fileSize = fileSize,
            downloadUrl = downloadUrl,
            createdAt = createdAt,
            completedAt = completedAt!!,
            localPath = localPath!!,
        )

        DownloadState.STATE_FAILED -> DownloadState.Failed(
            id = id,
            artifactId = artifactId,
            buildId = buildId,
            projectId = projectId,
            fileName = fileName,
            fileSize = fileSize,
            downloadUrl = downloadUrl,
            createdAt = createdAt,
            errorMessage = errorMessage ?: "Unknown error",
        )

        else -> throw IllegalStateException("Unknown download state: $state")
    }
}