package com.davidmedenjak.indiana.download

import androidx.compose.runtime.Immutable
import java.time.Instant

@Immutable
sealed class DownloadState {
    abstract val id: String
    abstract val artifactId: String
    abstract val buildId: String
    abstract val projectId: String
    abstract val fileName: String
    abstract val fileSize: Long
    abstract val downloadUrl: String
    abstract val createdAt: Instant

    data class Pending(
        override val id: String,
        override val artifactId: String,
        override val buildId: String,
        override val projectId: String,
        override val fileName: String,
        override val fileSize: Long,
        override val downloadUrl: String,
        override val createdAt: Instant,
    ) : DownloadState()

    data class InProgress(
        override val id: String,
        override val artifactId: String,
        override val buildId: String,
        override val projectId: String,
        override val fileName: String,
        override val fileSize: Long,
        override val downloadUrl: String,
        override val createdAt: Instant,
        val downloadedBytes: Long,
    ) : DownloadState() {
        val progress: Float get() = if (fileSize > 0) downloadedBytes.toFloat() / fileSize else 0f
        val isIndeterminate: Boolean get() = fileSize <= 0
    }

    data class Completed(
        override val id: String,
        override val artifactId: String,
        override val buildId: String,
        override val projectId: String,
        override val fileName: String,
        override val fileSize: Long,
        override val downloadUrl: String,
        override val createdAt: Instant,
        val completedAt: Instant,
        val localPath: String,
    ) : DownloadState()

    data class Failed(
        override val id: String,
        override val artifactId: String,
        override val buildId: String,
        override val projectId: String,
        override val fileName: String,
        override val fileSize: Long,
        override val downloadUrl: String,
        override val createdAt: Instant,
        val errorMessage: String,
    ) : DownloadState()

    companion object {
        const val STATE_PENDING = "pending"
        const val STATE_IN_PROGRESS = "in_progress"
        const val STATE_COMPLETED = "completed"
        const val STATE_FAILED = "failed"
    }
}