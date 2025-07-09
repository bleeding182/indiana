package com.davidmedenjak.indiana.download

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileOpener @Inject constructor() {

    fun openFile(context: Context, localPath: String): Boolean {
        val fileName = File(localPath).name
        return openFile(context, localPath, fileName)
    }

    fun installApk(context: Context, localPath: String): Boolean {
        return try {
            val uri = getFileUri(context, localPath)
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            // Directly start the APK installer without chooser
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                showNoAppFoundMessage(context, File(localPath).name)
                false
            }
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            showErrorMessage(context, File(localPath).name, e.message)
            false
        }
    }

    fun openFile(context: Context, localPath: String, fileName: String): Boolean {
        return try {
            val uri = getFileUri(context, localPath)
            val mimeType = getMimeType(fileName)
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            // Check if there's an app that can handle this intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                // Try with intent chooser for broader app support
                val chooserIntent = Intent.createChooser(intent, "Open with")
                chooserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                
                if (chooserIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(chooserIntent)
                    true
                } else {
                    showNoAppFoundMessage(context, fileName)
                    false
                }
            }
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            showErrorMessage(context, fileName, e.message)
            false
        }
    }

    private fun getFileUri(context: Context, localPath: String): Uri {
        return if (localPath.startsWith("content://") || localPath.startsWith("file://")) {
            localPath.toUri()
        } else {
            val file = File(localPath)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Use FileProvider for API 24+
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                Uri.fromFile(file)
            }
        }
    }

    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".apk", ignoreCase = true) -> "application/vnd.android.package-archive"
            fileName.endsWith(".aab", ignoreCase = true) -> "application/octet-stream"
            fileName.endsWith(".zip", ignoreCase = true) -> "application/zip"
            fileName.endsWith(".json", ignoreCase = true) -> "application/json"
            fileName.endsWith(".txt", ignoreCase = true) -> "text/plain"
            fileName.endsWith(".log", ignoreCase = true) -> "text/plain"
            fileName.endsWith(".xml", ignoreCase = true) -> "text/xml"
            fileName.endsWith(".html", ignoreCase = true) -> "text/html"
            fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            fileName.endsWith(".png", ignoreCase = true) -> "image/png"
            fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
            fileName.endsWith(".mp4", ignoreCase = true) -> "video/mp4"
            fileName.endsWith(".mp3", ignoreCase = true) -> "audio/mpeg"
            else -> "*/*" // Generic type for unknown files
        }
    }

    private fun showNoAppFoundMessage(context: Context, fileName: String) {
        Toast.makeText(
            context,
            "No app found to open ${fileName}",
            Toast.LENGTH_LONG
        ).show()
        Firebase.crashlytics.recordException(
            IllegalStateException("No app found to open file: $fileName")
        )
    }

    private fun showErrorMessage(context: Context, fileName: String, errorMessage: String?) {
        Toast.makeText(
            context,
            "Error opening $fileName: ${errorMessage ?: "Unknown error"}",
            Toast.LENGTH_LONG
        ).show()
    }
}