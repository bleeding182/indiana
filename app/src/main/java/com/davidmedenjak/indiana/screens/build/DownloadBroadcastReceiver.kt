package com.davidmedenjak.indiana.features.artifacts

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import java.io.File
import androidx.core.net.toUri
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

class DownloadBroadcastReceiver(val downloadId: Long) : BroadcastReceiver() {
    override fun onReceive(context: Context, data: Intent) {
        val id = data.getLongExtra("extra_download_id", -1)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        if (id == downloadId) {
            context.unregisterReceiver(this)

            val query = DownloadManager.Query()
            query.setFilterById(id)
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {

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

                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Could not open file ${uri.lastPathSegment}", Toast.LENGTH_SHORT).show()
                    Firebase.crashlytics
                        .recordException(IllegalStateException("Could not open file."))
                }
            }
        }
    }
}