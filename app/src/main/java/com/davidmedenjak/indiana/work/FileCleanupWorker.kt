package com.davidmedenjak.indiana.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.davidmedenjak.indiana.app.UserSettings
import com.davidmedenjak.indiana.db.DownloadDao
import com.davidmedenjak.indiana.settings.CleanupPeriod
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.time.Instant
import javax.inject.Inject

@HiltWorker
class FileCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val downloadDao: DownloadDao,
    private val userSettings: UserSettings
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            cleanupOldDownloads()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun cleanupOldDownloads() {
        val cleanupSettings = userSettings.downloadCleanupSettings
        
        // Skip cleanup if disabled or set to never
        if (!cleanupSettings.isEnabled || cleanupSettings.cleanupPeriod == CleanupPeriod.NEVER) {
            return
        }
        
        val cutoffTime = Instant.now().minusSeconds(cleanupSettings.cleanupPeriod.days * 24 * 60 * 60L)
        
        // Get old completed downloads with files
        val oldCompletedDownloads = downloadDao.getOldCompletedDownloadsWithFiles(cutoffTime)
        
        // Get old failed downloads with files
        val oldFailedDownloads = downloadDao.getOldFailedDownloadsWithFiles(cutoffTime)
        
        // Delete files and database entries for completed downloads
        oldCompletedDownloads.forEach { download ->
            download.localPath?.let { path ->
                try {
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    // Log error but continue with cleanup
                }
            }
        }
        
        // Delete files and database entries for failed downloads
        oldFailedDownloads.forEach { download ->
            download.localPath?.let { path ->
                try {
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    // Log error but continue with cleanup
                }
            }
        }
        
        // Clean up database entries
        downloadDao.deleteOldCompletedDownloads(cutoffTime)
        downloadDao.deleteOldFailedDownloads(cutoffTime)
        
        // Update last cleanup time
        val updatedSettings = cleanupSettings.copy(lastCleanupTime = Instant.now())
        userSettings.downloadCleanupSettings = updatedSettings
    }
}