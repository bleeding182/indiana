package com.davidmedenjak.indiana.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import androidx.room.Room
import com.davidmedenjak.indiana.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant

/**
 * Manifest-registered broadcast receiver that handles download completion events
 * even when the app is closed. This ensures our database stays in sync with
 * the system download manager.
 */
class DownloadCompletionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            return
        }

        val systemDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        if (systemDownloadId == -1L) {
            return
        }

        Log.d("DownloadCompletionReceiver", "Received download completion for ID: $systemDownloadId")

        // Use goAsync() to allow background processing
        val pendingResult = goAsync()
        
        scope.launch {
            try {
                handleDownloadCompletion(context, systemDownloadId)
            } catch (e: Exception) {
                Log.e("DownloadCompletionReceiver", "Error handling download completion", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleDownloadCompletion(context: Context, systemDownloadId: Long) {
        val database = getDatabase(context)
        val downloadDao = database.downloads()
        
        // Find our download record by system download ID
        val downloadEntity = downloadDao.getBySystemDownloadId(systemDownloadId)
        if (downloadEntity == null) {
            Log.w("DownloadCompletionReceiver", "No download found for system ID: $systemDownloadId")
            return
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(systemDownloadId)
        val cursor = downloadManager.query(query)

        if (cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = cursor.getInt(statusIndex)

            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    val uri = getDownloadUri(cursor)
                    downloadDao.updateCompleted(
                        id = downloadEntity.id,
                        state = DownloadState.STATE_COMPLETED,
                        completedAt = Instant.now(),
                        localPath = uri.toString()
                    )
                    Log.d("DownloadCompletionReceiver", "Download completed: ${downloadEntity.id}")
                }
                DownloadManager.STATUS_FAILED -> {
                    val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                    val reason = cursor.getInt(reasonIndex)
                    val errorMessage = getErrorMessage(reason)
                    downloadDao.updateFailed(
                        id = downloadEntity.id,
                        state = DownloadState.STATE_FAILED,
                        errorMessage = errorMessage
                    )
                    Log.d("DownloadCompletionReceiver", "Download failed: ${downloadEntity.id} - $errorMessage")
                }
            }
        }
        cursor.close()
    }

    private fun getDownloadUri(cursor: android.database.Cursor): Uri {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // For older versions, use the local file path
            @Suppress("DEPRECATION")
            val localPathIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)
            val downloadedPath = cursor.getString(localPathIndex)
            Uri.fromFile(File(downloadedPath))
        } else {
            // For newer versions, use the local URI
            val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            cursor.getString(uriIndex).toUri()
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

    private fun getDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "indiana"
        ).build()
    }
}