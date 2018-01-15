package com.davidmedenjak.indiana.features.artifacts

import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.api.Artifact
import com.davidmedenjak.indiana.api.BitriseApi
import com.davidmedenjak.indiana.di.PerActivity
import javax.inject.Inject

@PerActivity
class ArtifactAdapter @Inject constructor(val api: BitriseApi) : RecyclerView.Adapter<ArtifactViewHolder>() {

    var projectSlug: String = ""
    var buildSlug: String = ""

    var artifacts: List<Artifact> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = artifacts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtifactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_artifact, parent, false)
        return ArtifactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtifactViewHolder, position: Int) {
        val artifact = artifacts[position]
        holder.title.text = artifact.title

        holder.itemView.setOnClickListener {
            api.fetchArtifact(projectSlug, buildSlug, artifact.slug)
                    .subscribe({
                        val context = holder.itemView.context
                        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
                            val request = DownloadManager.Request(Uri.parse(it.data.downloadUrl))
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    .setTitle(it.data.title)
                            downloadManager.enqueue(request)
                        } else {
                            val request = DownloadManager.Request(Uri.parse(it.data.downloadUrl))
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                                    .setTitle(it.data.title)

                            val downloadId = downloadManager.enqueue(request)
                            context.registerReceiver(DownloadBroadcastReceiver(downloadId), IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                        }
                    }, {

                    })
        }
    }

}