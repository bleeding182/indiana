package com.davidmedenjak.indiana.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "download",
    indices = [
        Index("artifact_id"),
        Index("build_id"),
        Index("project_id"),
        Index("state"),
        Index("created_at")
    ]
)
data class DownloadEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "artifact_id") val artifactId: String,
    @ColumnInfo(name = "build_id") val buildId: String,
    @ColumnInfo(name = "project_id") val projectId: String,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "file_size") val fileSize: Long,
    @ColumnInfo(name = "downloaded_bytes") val downloadedBytes: Long,
    @ColumnInfo(name = "download_url") val downloadUrl: String,
    @ColumnInfo(name = "local_path") val localPath: String?,
    @ColumnInfo(name = "state") val state: String,
    @ColumnInfo(name = "system_download_id") val systemDownloadId: Long?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "completed_at") val completedAt: Instant?,
    @ColumnInfo(name = "error_message") val errorMessage: String?
)