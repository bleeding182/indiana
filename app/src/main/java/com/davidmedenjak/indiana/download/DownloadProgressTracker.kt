package com.davidmedenjak.indiana.download

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadProgressTracker @Inject constructor(
    private val context: Application,
    private val downloadRepository: DownloadRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val activeTrackers = mutableMapOf<String, ProgressObserver>()

    fun startTracking(downloadId: String, systemDownloadId: Long) {
        if (activeTrackers.containsKey(downloadId)) {
            return
        }

        val observer = ProgressObserver(downloadId, systemDownloadId)
        activeTrackers[downloadId] = observer
        
        observer.startObserving()
        
        Log.d("DownloadProgressTracker", "Started tracking download: $downloadId")
    }

    fun stopTracking(downloadId: String) {
        activeTrackers.remove(downloadId)?.let { observer ->
            observer.cleanup()
            Log.d("DownloadProgressTracker", "Stopped tracking download: $downloadId")
        }
    }

    fun stopTrackingBySystemId(systemDownloadId: Long) {
        // Find and stop tracking by system download ID
        activeTrackers.entries.removeAll { (downloadId, observer) ->
            if (observer.systemDownloadId == systemDownloadId) {
                observer.cleanup()
                Log.d("DownloadProgressTracker", "Stopped tracking download by system ID: $downloadId")
                true
            } else {
                false
            }
        }
    }

    fun cleanup() {
        activeTrackers.values.forEach { it.cleanup() }
        activeTrackers.clear()
        scope.cancel()
    }

    private inner class ProgressObserver(
        private val downloadId: String,
        val systemDownloadId: Long,
    ) : ContentObserver(Handler(Looper.getMainLooper())) {

        private var cursor: Cursor? = null
        
        fun startObserving() {
            val query = DownloadManager.Query().setFilterById(systemDownloadId)
            cursor = downloadManager.query(query)
            cursor?.registerContentObserver(this)
        }

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            
            scope.launch {
                try {
                    // Create a fresh query instead of requerying existing cursor
                    val query = DownloadManager.Query().setFilterById(systemDownloadId)
                    val freshCursor = downloadManager.query(query)
                    
                    freshCursor?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val downloadedBytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            val totalSizeIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            
                            if (downloadedBytesIndex >= 0 && totalSizeIndex >= 0 && statusIndex >= 0) {
                                val downloadedBytes = cursor.getLong(downloadedBytesIndex)
                                val totalSize = cursor.getLong(totalSizeIndex)
                                val status = cursor.getInt(statusIndex)
                                
                                when (status) {
                                    DownloadManager.STATUS_PENDING -> {
                                        // Download is queued but not yet started
                                        Log.d("DownloadProgressTracker", "Download pending: $downloadId")
                                    }
                                    DownloadManager.STATUS_RUNNING -> {
                                        downloadRepository.markInProgress(downloadId)
                                        downloadRepository.updateProgress(downloadId, downloadedBytes)
                                        Log.d("DownloadProgressTracker", "Progress update: $downloadId - $downloadedBytes/$totalSize")
                                    }
                                    DownloadManager.STATUS_SUCCESSFUL -> {
                                        val localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                                        val localUri = if (localUriIndex >= 0) cursor.getString(localUriIndex) else ""
                                        downloadRepository.markCompleted(downloadId, localUri ?: "")
                                        stopTracking(downloadId)
                                        Log.d("DownloadProgressTracker", "Download completed: $downloadId")
                                    }
                                    DownloadManager.STATUS_FAILED -> {
                                        val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                                        val reason = if (reasonIndex >= 0) cursor.getInt(reasonIndex) else DownloadManager.ERROR_UNKNOWN
                                        val errorMessage = getErrorMessage(reason)
                                        downloadRepository.markFailed(downloadId, errorMessage)
                                        stopTracking(downloadId)
                                        Log.d("DownloadProgressTracker", "Download failed: $downloadId - $errorMessage")
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DownloadProgressTracker", "Error updating progress for $downloadId", e)
                    // Don't immediately mark as failed - the download might still be active
                    // The DownloadCompletionReceiver will handle final state updates
                }
            }
        }

        fun cleanup() {
            cursor?.let { c ->
                try {
                    c.unregisterContentObserver(this)
                    c.close()
                } catch (e: Exception) {
                    Log.w("DownloadProgressTracker", "Error cleaning up cursor for $downloadId", e)
                }
            }
            cursor = null
        }
    }

    private fun getErrorMessage(reason: Int): String {
        return when (reason) {
            DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume download"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Device not found"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
            DownloadManager.ERROR_FILE_ERROR -> "File error"
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data error"
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient space"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP code"
            DownloadManager.ERROR_UNKNOWN -> "Unknown error"
            else -> "Download failed (code: $reason)"
        }
    }
}