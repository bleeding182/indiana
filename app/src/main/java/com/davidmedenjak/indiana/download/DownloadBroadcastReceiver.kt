package com.davidmedenjak.indiana.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.net.toUri
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class DownloadBroadcastReceiver(
    private val downloadId: String,
    private val systemDownloadId: Long,
    private val downloadRepository: DownloadRepository,
) : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        
        if (id == systemDownloadId) {
            context.unregisterReceiver(this)
            
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(id)
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = cursor.getInt(statusIndex)
                
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        handleSuccessfulDownload(context, cursor)
                    }
                    DownloadManager.STATUS_FAILED -> {
                        val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                        val reason = cursor.getInt(reasonIndex)
                        handleFailedDownload(reason)
                    }
                }
            }
            cursor.close()
        }
    }

    private fun handleSuccessfulDownload(context: Context, cursor: android.database.Cursor) {
        scope.launch {
            try {
                val uri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    // we need the file path on < N
                    // https://stackoverflow.com/a/41663453/1837367
                    @Suppress("DEPRECATION")
                    val localPathIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)
                    val downloadedPath = cursor.getString(localPathIndex)
                    Uri.fromFile(File(downloadedPath))
                } else {
                    val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                    cursor.getString(uriIndex).toUri()
                }

                // Update database with completed status
                downloadRepository.markCompleted(downloadId, uri.toString())

                // Try to open the file (for APK files)
                if (shouldAutoOpenFile(uri)) {
                    openFile(context, uri)
                }

            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                downloadRepository.markFailed(downloadId, "Post-download processing failed: ${e.message}")
            }
        }
    }

    private fun handleFailedDownload(reason: Int) {
        scope.launch {
            val errorMessage = when (reason) {
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
            
            downloadRepository.markFailed(downloadId, errorMessage)
        }
    }

    private fun shouldAutoOpenFile(uri: Uri): Boolean {
        // Only auto-open APK files
        return uri.lastPathSegment?.endsWith(".apk", ignoreCase = true) == true
    }

    private fun openFile(context: Context, uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(
                    context,
                    "Could not open file ${uri.lastPathSegment}",
                    Toast.LENGTH_SHORT
                ).show()
                Firebase.crashlytics.recordException(
                    IllegalStateException("Could not open file: ${uri.lastPathSegment}")
                )
            }
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            Toast.makeText(
                context,
                "Error opening file: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}