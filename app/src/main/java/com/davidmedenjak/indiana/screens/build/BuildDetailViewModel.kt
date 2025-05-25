package com.davidmedenjak.indiana.screens.build

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.davidmedenjak.indiana.api.BuildArtifactApi
import com.davidmedenjak.indiana.features.artifacts.DownloadBroadcastReceiver
import com.davidmedenjak.indiana.model.V0ArtifactListElementResponseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BuildDetailViewModel @Inject constructor(
    private val application: Application,
    private val api: BuildArtifactApi,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    lateinit var navKey: BuildDetailGraph

    val pagedArtifacts = Pager(
        PagingConfig(pageSize = 15, initialLoadSize = 20)
    ) {
        BuildDetailPagingSource(
            api,
            appSlug = navKey.appSlug,
            buildSlug = navKey.buildSlug
        )
    }.flow.cachedIn(viewModelScope)

    suspend fun downloadArtifact(artifact: V0ArtifactListElementResponseModel) {
        val details = api.artifactShow(
            navKey.appSlug,
            buildSlug = navKey.buildSlug,
            artifactSlug = artifact.slug ?: return,
            download = null,
        ).data ?: return
        val context = application
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadUrl = details.expiringDownloadUrl?.toUri() ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            val request = DownloadManager.Request(downloadUrl)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setTitle(artifact.title)
            downloadManager.enqueue(request)
        } else {
            val request = DownloadManager.Request(downloadUrl)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setTitle(artifact.title)

            val downloadId = downloadManager.enqueue(request)
            ContextCompat.registerReceiver(
                context,
                DownloadBroadcastReceiver(downloadId),
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }
}

class BuildDetailPagingSource(
    val api: BuildArtifactApi,
    val appSlug: String,
    val buildSlug: String,
) : PagingSource<String, V0ArtifactListElementResponseModel>() {
    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, V0ArtifactListElementResponseModel> {
        try {
            val response = api.artifactList(
                next = params.key,
                appSlug = appSlug,
                buildSlug = buildSlug,
                limit = params.loadSize
            )

            return LoadResult.Page(
                data = response.data ?: emptyList(),
                prevKey = null,
                nextKey = response.paging?.next,
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, V0ArtifactListElementResponseModel>): String? {
        return null
    }
}
