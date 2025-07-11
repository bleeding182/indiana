package com.davidmedenjak.indiana.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadEntity)

    @Update
    suspend fun update(download: DownloadEntity)

    @Delete
    suspend fun delete(download: DownloadEntity)

    @Query("SELECT * FROM download WHERE id = :id")
    suspend fun getById(id: String): DownloadEntity?

    @Query("SELECT * FROM download WHERE id = :id")
    fun getByIdFlow(id: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM download WHERE artifact_id = :artifactId ORDER BY created_at DESC LIMIT 1")
    suspend fun getByArtifactId(artifactId: String): DownloadEntity?

    @Query("SELECT * FROM download WHERE artifact_id = :artifactId ORDER BY created_at DESC LIMIT 1")
    fun getByArtifactIdFlow(artifactId: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM download WHERE artifact_id = :artifactId ORDER BY created_at DESC")
    suspend fun getAllByArtifactId(artifactId: String): List<DownloadEntity>

    @Query("SELECT * FROM download WHERE state = :state ORDER BY created_at DESC")
    fun getByState(state: String): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM download WHERE state IN ('pending', 'in_progress') ORDER BY created_at DESC")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM download ORDER BY created_at DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM download ORDER BY created_at DESC")
    suspend fun getAllDownloadsList(): List<DownloadEntity>

    @Query("SELECT * FROM download WHERE project_id = :projectId ORDER BY created_at DESC")
    fun getDownloadsByProject(projectId: String): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM download WHERE build_id = :buildId ORDER BY created_at DESC")
    fun getDownloadsByBuild(buildId: String): Flow<List<DownloadEntity>>

    @Query("UPDATE download SET downloaded_bytes = :downloadedBytes WHERE id = :id")
    suspend fun updateProgress(id: String, downloadedBytes: Long)

    @Query("UPDATE download SET state = :state, completed_at = :completedAt, local_path = :localPath WHERE id = :id")
    suspend fun updateCompleted(id: String, state: String, completedAt: java.time.Instant, localPath: String?)

    @Query("UPDATE download SET state = :state, error_message = :errorMessage WHERE id = :id")
    suspend fun updateFailed(id: String, state: String, errorMessage: String)

    @Query("UPDATE download SET system_download_id = :systemDownloadId WHERE id = :id")
    suspend fun updateSystemDownloadId(id: String, systemDownloadId: Long)

    @Query("SELECT * FROM download WHERE system_download_id = :systemDownloadId")
    suspend fun getBySystemDownloadId(systemDownloadId: Long): DownloadEntity?

    @Query("SELECT * FROM download WHERE state IN ('pending', 'in_progress') AND system_download_id IS NOT NULL")
    suspend fun getActiveDownloadsWithSystemId(): List<DownloadEntity>


    @Query("DELETE FROM download WHERE state = 'completed' AND completed_at < :cutoffTime")
    suspend fun deleteOldCompletedDownloads(cutoffTime: java.time.Instant)

    @Query("DELETE FROM download WHERE state = 'failed' AND created_at < :cutoffTime")
    suspend fun deleteOldFailedDownloads(cutoffTime: java.time.Instant)

    @Query("SELECT * FROM download WHERE state = 'completed' AND completed_at < :cutoffTime AND local_path IS NOT NULL")
    suspend fun getOldCompletedDownloadsWithFiles(cutoffTime: java.time.Instant): List<DownloadEntity>

    @Query("SELECT * FROM download WHERE state = 'failed' AND created_at < :cutoffTime AND local_path IS NOT NULL")
    suspend fun getOldFailedDownloadsWithFiles(cutoffTime: java.time.Instant): List<DownloadEntity>
}