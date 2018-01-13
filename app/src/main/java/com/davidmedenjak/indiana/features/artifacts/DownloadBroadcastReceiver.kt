package com.davidmedenjak.indiana.features.artifacts

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri

class DownloadBroadcastReceiver(val downloadId : Long) : BroadcastReceiver() {
    override fun onReceive(context: Context, data: Intent) {
        val id = data.getLongExtra("extra_download_id", -1)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        if (id == downloadId) {
            context.unregisterReceiver(this)

            val query = DownloadManager.Query()
            query.setFilterById(id)
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {
                val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                val localUri = Uri.parse(cursor.getString(uriIndex))

                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(localUri, "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.startActivity(intent)
            }
        }
    }

}